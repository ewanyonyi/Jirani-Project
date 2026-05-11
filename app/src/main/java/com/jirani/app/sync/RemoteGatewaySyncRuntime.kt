package com.jirani.app.sync

import com.jirani.app.BuildConfig
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.data.local.ReceivedReportItem
import com.jirani.app.data.local.RemoteGatewaySyncPolicy
import com.jirani.app.data.local.ReportSensitivity
import com.jirani.app.data.local.ReportingSyncPolicy
import com.jirani.app.data.local.SanitizedReportPayload
import com.jirani.app.data.local.SyncEnvelope
import com.jirani.app.data.local.SyncTransport
import com.jirani.app.data.local.VerificationStatus
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

object RemoteGatewaySyncRuntime {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = RemoteGatewayClient(BuildConfig.JIRANI_REMOTE_GATEWAY_URL)
    private var started = false
    private var uploading = false

    fun initialize() {
        if (started) return
        started = true
        scope.launch {
            uploadPendingReports()
        }
        scope.launch {
            LocalFirstUiStore.network.collect { snapshot ->
                if (snapshot.remoteGatewayEnvelopes.isNotEmpty()) {
                    uploadPendingReports()
                }
            }
        }
    }

    fun retryNow() {
        scope.launch {
            uploadPendingReports()
        }
    }

    private suspend fun uploadPendingReports() {
        if (uploading) return
        uploading = true
        try {
            LocalFirstUiStore.pendingRemoteGatewayEnvelopes().forEach { envelope ->
                val result = client.upload(envelope)
                if (result.uploaded) {
                    LocalFirstUiStore.markRemoteGatewayUploadSucceeded(envelope.envelopeId)
                } else {
                    LocalFirstUiStore.markRemoteGatewayUploadFailed(envelope.envelopeId, result.message)
                    delay(RetryRestMillis)
                }
            }
            LocalFirstUiStore.receiveRemoteGatewayReports(client.downloadAvailableReports())
        } finally {
            uploading = false
        }
    }

    private const val RetryRestMillis = 30_000L
}

data class RemoteGatewayUploadResult(
    val uploaded: Boolean,
    val message: String,
)

class RemoteGatewayClient(
    private val baseUrl: String,
) {
    suspend fun upload(envelope: SyncEnvelope): RemoteGatewayUploadResult =
        withContext(Dispatchers.IO) {
            RemoteGatewaySyncPolicy.remoteGatewayBlockReason(envelope)?.let { reason ->
                return@withContext RemoteGatewayUploadResult(uploaded = false, message = reason)
            }

            val endpoint = baseUrl.trimEnd('/') + "/sync/envelopes"
            if (!endpoint.isPrivateEnoughForGateway()) {
                return@withContext RemoteGatewayUploadResult(
                    uploaded = false,
                    message = "Remote Rust gateway must use HTTPS outside local emulator testing.",
                )
            }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TimeoutMillis
                readTimeout = TimeoutMillis
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                addPrivacyHeaders()
            }

            try {
                val body = envelope.toRemoteGatewayJson().toString().toByteArray(Charsets.UTF_8)
                connection.outputStream.use { it.write(body) }
                val code = connection.responseCode
                if (code in 200..299 || code == HttpURLConnection.HTTP_CONFLICT) {
                    RemoteGatewayUploadResult(
                        uploaded = true,
                        message = "Anonymized report uploaded to Rust gateway.",
                    )
                } else {
                    RemoteGatewayUploadResult(
                        uploaded = false,
                        message = "Rust gateway rejected the anonymized report with HTTP $code.",
                    )
                }
            } catch (error: IOException) {
                RemoteGatewayUploadResult(
                    uploaded = false,
                    message = "Rust gateway unavailable; anonymized report will retry later.",
                )
            } finally {
                connection.disconnect()
            }
        }

    suspend fun downloadAvailableReports(): List<ReceivedReportItem> =
        withContext(Dispatchers.IO) {
            val endpoint = baseUrl.trimEnd('/') + "/sync/envelopes"
            if (!endpoint.isPrivateEnoughForGateway()) {
                return@withContext emptyList()
            }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TimeoutMillis
                readTimeout = TimeoutMillis
                setRequestProperty("Accept", "application/json")
                addPrivacyHeaders()
            }

            try {
                val code = connection.responseCode
                if (code !in 200..299) return@withContext emptyList()
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseDownloadedReports(body)
            } catch (error: IOException) {
                emptyList()
            } finally {
                connection.disconnect()
            }
        }

    private fun SyncEnvelope.toRemoteGatewayJson(): JSONObject =
        JSONObject()
            .put("envelopeId", envelopeId)
            .put("recordType", recordType)
            .put("recordId", recordId)
            .put("contentHash", contentHash)
            .put("version", version)
            .put("lastModifiedBucket", lastModifiedBucket)
            .put("audienceTier", audienceTier.name)
            .put("expiresAtEpochSeconds", expiresAtEpochSeconds)
            .put("payload", JSONObject()
                .put("reportType", payload.reportType)
                .put("generalArea", payload.generalArea)
                .put("timeWindow", payload.timeWindow)
                .put("submittedAtEpochSeconds", payload.submittedAtEpochSeconds)
                .put("observedRisk", payload.observedRisk)
                .put("verificationStatus", payload.verificationStatus.name)
                .put("sensitivity", payload.sensitivity.name))

    private fun parseDownloadedReports(body: String): List<ReceivedReportItem> {
        val trimmed = body.trim()
        if (trimmed.isBlank()) return emptyList()

        val envelopes = if (trimmed.startsWith("[")) {
            org.json.JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("envelopes") ?: org.json.JSONArray()
        }

        return (0 until envelopes.length()).mapNotNull { index ->
            envelopes.optJSONObject(index)?.toReceivedReportItem()
        }
    }

    private fun JSONObject.toReceivedReportItem(): ReceivedReportItem? {
        val payloadJson = optJSONObject("payload") ?: return null
        val payload = SanitizedReportPayload(
            reportType = payloadJson.optString("reportType", "unknown report"),
            generalArea = payloadJson.optString("generalArea", "general area withheld"),
            timeWindow = payloadJson.optString("timeWindow", "time window not specified"),
            submittedAtEpochSeconds = payloadJson.optLong("submittedAtEpochSeconds"),
            observedRisk = payloadJson.optString("observedRisk", "details withheld"),
            verificationStatus = payloadJson.optEnum("verificationStatus", VerificationStatus.PendingVerification),
            sensitivity = payloadJson.optEnum("sensitivity", ReportSensitivity.Community),
        )
        val contentHash = optString("contentHash")
        if (contentHash.isBlank() || ReportingSyncPolicy.payloadHash(payload) != contentHash) return null

        val envelopeId = optString("envelopeId").ifBlank { contentHash.take(12) }
        return ReceivedReportItem(
            packetId = "remote-$envelopeId",
            fromAlias = "Rust gateway",
            transport = SyncTransport.RemoteRustGateway,
            reportType = payload.reportType,
            generalArea = payload.generalArea,
            timeWindow = payload.timeWindow,
            submittedAtEpochSeconds = payload.submittedAtEpochSeconds,
            observedRisk = payload.observedRisk,
            verificationStatus = payload.verificationStatus,
            sensitivity = payload.sensitivity,
        )
    }

    private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T =
        runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(fallback)

    private fun String.isPrivateEnoughForGateway(): Boolean {
        val url = runCatching { URL(this) }.getOrNull() ?: return false
        if (url.protocol == "https") return true
        return url.protocol == "http" && url.host in LocalDevelopmentHosts
    }

    private fun HttpURLConnection.addPrivacyHeaders() {
        setRequestProperty("User-Agent", StableUserAgent)
        setRequestProperty("Cache-Control", "no-store")
        setRequestProperty("Pragma", "no-cache")
        setRequestProperty("X-Jirani-Privacy", "minimized-envelope-v1")
        val token = BuildConfig.JIRANI_REMOTE_GATEWAY_TOKEN.trim()
        if (token.isNotEmpty()) {
            setRequestProperty("Authorization", "Bearer $token")
        }
    }

    private companion object {
        const val TimeoutMillis = 5_000
        const val StableUserAgent = "Jirani-Android-Sync/1"
        val LocalDevelopmentHosts = setOf("10.0.2.2", "localhost", "127.0.0.1")
    }
}
