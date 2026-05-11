package com.jirani.app.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

enum class ReportSensitivity {
    Community,
    Protection,
    SurvivorCentered,
}

enum class VerificationStatus {
    LocalOnly,
    PendingVerification,
    VerifierConfirmed,
    CommunityAlertReady,
    RejectedOrExpired,
}

enum class SyncAudienceTier {
    TrustedVerifier,
    ProtectionActors,
    SurvivorSupportOnly,
    CommunityAlert,
}

enum class SyncTransport {
    NearbyConnections,
    WifiDirect,
    AndroidShareSheet,
    QrOrEncryptedFile,
    RemoteRustGateway,
}

enum class SyncState {
    LocalHold,
    WaitingForTrustedPeer,
    ReadyForNearbyShare,
    SharedToTrustedPeer,
}

data class SafetyReportRecord(
    val recordId: String,
    val reportType: String,
    val generalLocation: String,
    val timeWindow: String,
    val incidentSummary: String,
    val sensitivity: ReportSensitivity,
    val verificationStatus: VerificationStatus = VerificationStatus.LocalOnly,
    val createdAtEpochSeconds: Long = Instant.now().epochSecond,
    val expiresAtEpochSeconds: Long,
)

data class SanitizedReportPayload(
    val reportType: String,
    val generalArea: String,
    val timeWindow: String,
    val submittedAtEpochSeconds: Long,
    val observedRisk: String,
    val verificationStatus: VerificationStatus,
    val sensitivity: ReportSensitivity,
)

data class SyncEnvelope(
    val envelopeId: String,
    val recordType: String,
    val recordId: String,
    val contentHash: String,
    val version: Int,
    val lastModifiedBucket: String,
    val audienceTier: SyncAudienceTier,
    val syncState: SyncState,
    val allowedTransports: List<SyncTransport>,
    val payload: SanitizedReportPayload,
    val expiresAtEpochSeconds: Long,
    val deliveredDeviceAliases: List<String> = emptyList(),
    val sendingDeviceAliases: List<String> = emptyList(),
    val maxUniqueDevices: Int = 5,
)

fun SyncEnvelope.isStale(nowEpochSeconds: Long = Instant.now().epochSecond): Boolean =
    nowEpochSeconds >= expiresAtEpochSeconds

object RemoteGatewaySyncPolicy {
    fun canUploadToRemoteGateway(
        envelope: SyncEnvelope,
        nowEpochSeconds: Long = Instant.now().epochSecond,
    ): Boolean =
        remoteGatewayBlockReason(envelope, nowEpochSeconds) == null

    fun remoteGatewayBlockReason(
        envelope: SyncEnvelope,
        nowEpochSeconds: Long = Instant.now().epochSecond,
    ): String? = when {
        envelope.isStale(nowEpochSeconds) -> "Report is stale and will not be uploaded."
        envelope.payload.sensitivity == ReportSensitivity.SurvivorCentered -> "Survivor-centered reports stay out of remote gateway sync."
        envelope.audienceTier == SyncAudienceTier.SurvivorSupportOnly -> "Survivor-support reports require explicit private handoff."
        SyncTransport.RemoteRustGateway !in envelope.allowedTransports -> "Remote gateway is not allowed for this report sensitivity."
        else -> null
    }

    fun uploadStatusLabel(envelope: SyncEnvelope): String =
        remoteGatewayBlockReason(envelope) ?: "Waiting for Rust gateway"
}

data class NearbyJiraniDevice(
    val deviceAlias: String,
    val trusted: Boolean,
    val supportedTransports: List<SyncTransport>,
)

data class WireReportPacket(
    val packetId: String,
    val sourceEnvelopeId: String,
    val targetAlias: String,
    val transport: SyncTransport,
    val audienceTier: SyncAudienceTier,
    val contentHash: String,
    val sealedPayload: String,
)

data class ReceivedReportItem(
    val packetId: String,
    val fromAlias: String,
    val transport: SyncTransport,
    val reportType: String,
    val generalArea: String,
    val timeWindow: String,
    val submittedAtEpochSeconds: Long,
    val observedRisk: String,
    val verificationStatus: VerificationStatus,
    val sensitivity: ReportSensitivity,
)

data class TransferResult(
    val delivered: Boolean,
    val message: String,
    val packet: WireReportPacket? = null,
    val receivedItem: ReceivedReportItem? = null,
)

data class TransferBatchResult(
    val deliveredCount: Int,
    val attemptedCount: Int,
    val updatedEnvelope: SyncEnvelope,
    val results: List<TransferResult>,
    val message: String,
)

object ReportingDeviceTransfer {
    private const val AesGcmTransformation = "AES/GCM/NoPadding"
    private const val GcmTagBits = 128
    private const val IvByteSize = 12

    fun sendToDevices(
        envelope: SyncEnvelope,
        targets: List<NearbyJiraniDevice>,
        nowEpochSeconds: Long = Instant.now().epochSecond,
    ): TransferBatchResult {
        if (envelope.isStale(nowEpochSeconds)) {
            return TransferBatchResult(
                deliveredCount = 0,
                attemptedCount = 0,
                updatedEnvelope = envelope,
                results = emptyList(),
                message = "Report is stale and will not be sent further.",
            )
        }

        val remainingSlots = envelope.maxUniqueDevices - envelope.deliveredDeviceAliases.size
        if (remainingSlots <= 0) {
            return TransferBatchResult(
                deliveredCount = 0,
                attemptedCount = 0,
                updatedEnvelope = envelope,
                results = emptyList(),
                message = "Report already reached ${envelope.maxUniqueDevices} unique devices.",
            )
        }

        val eligibleTargets = targets
            .filter { it.deviceAlias !in envelope.deliveredDeviceAliases }
            .take(remainingSlots)

        val results = eligibleTargets.map { target -> sendToDevice(envelope, target) }
        val deliveredAliases = results.mapNotNull { result ->
            result.packet?.targetAlias?.takeIf { result.delivered }
        }
        val updatedEnvelope = envelope.copy(
            deliveredDeviceAliases = envelope.deliveredDeviceAliases + deliveredAliases,
        )

        val message = when {
            deliveredAliases.isEmpty() && targets.isEmpty() -> "Report submitted. Waiting for a trusted nearby Jirani device."
            deliveredAliases.isEmpty() -> results.firstOrNull()?.message ?: "Report submitted. No eligible device accepted it yet."
            updatedEnvelope.deliveredDeviceAliases.size >= envelope.maxUniqueDevices ->
                "Report sent anonymously to ${updatedEnvelope.deliveredDeviceAliases.size}/${envelope.maxUniqueDevices} unique devices. Relay limit reached."
            else ->
                "Report sent anonymously to ${updatedEnvelope.deliveredDeviceAliases.size}/${envelope.maxUniqueDevices} unique devices. Scanning for more."
        }

        return TransferBatchResult(
            deliveredCount = deliveredAliases.size,
            attemptedCount = eligibleTargets.size,
            updatedEnvelope = updatedEnvelope,
            results = results,
            message = message,
        )
    }

    fun sendToDevice(
        envelope: SyncEnvelope,
        target: NearbyJiraniDevice,
    ): TransferResult {
        val prepared = createPacketForDevice(envelope, target)
        val packet = prepared.packet ?: return prepared

        return receivePacket(
            packet = packet,
            fromAlias = "This device",
        )
    }

    fun createPacketForDevice(
        envelope: SyncEnvelope,
        target: NearbyJiraniDevice,
    ): TransferResult {
        if (!target.trusted) {
            return TransferResult(
                delivered = false,
                message = "Nearby device is not trusted for report exchange.",
            )
        }

        if (envelope.syncState == SyncState.LocalHold && envelope.audienceTier == SyncAudienceTier.SurvivorSupportOnly) {
            return TransferResult(
                delivered = false,
                message = "Survivor-centered reports require explicit private handoff, not nearby broadcast.",
            )
        }

        val transport = envelope.allowedTransports.firstOrNull { it in target.supportedTransports }
            ?: return TransferResult(
                delivered = false,
                message = "No allowed transport is available for this report sensitivity.",
            )

        val sealedPayload = sealPayload(envelope.payload)
        val packet = WireReportPacket(
            packetId = "packet-${ReportingSyncPolicy.stableHash("${envelope.envelopeId}|${target.deviceAlias}|$sealedPayload").take(12)}",
            sourceEnvelopeId = envelope.envelopeId,
            targetAlias = target.deviceAlias,
            transport = transport,
            audienceTier = envelope.audienceTier,
            contentHash = envelope.contentHash,
            sealedPayload = sealedPayload,
        )

        return TransferResult(
            delivered = false,
            message = "Anonymized report packet is ready for ${target.deviceAlias}.",
            packet = packet,
        )
    }

    fun receivePacket(
        packet: WireReportPacket,
        fromAlias: String,
    ): TransferResult {
        val payload = try {
            unsealPayload(packet.sealedPayload)
        } catch (error: Exception) {
            return TransferResult(
                delivered = false,
                message = "Report packet could not be opened securely.",
                packet = packet,
            )
        }
        val expectedHash = ReportingSyncPolicy.payloadHash(payload)
        if (expectedHash != packet.contentHash) {
            return TransferResult(
                delivered = false,
                message = "Report packet failed integrity verification.",
                packet = packet,
            )
        }

        val item = ReceivedReportItem(
            packetId = packet.packetId,
            fromAlias = fromAlias,
            transport = packet.transport,
            reportType = payload.reportType,
            generalArea = payload.generalArea,
            timeWindow = payload.timeWindow,
            submittedAtEpochSeconds = payload.submittedAtEpochSeconds,
            observedRisk = payload.observedRisk,
            verificationStatus = payload.verificationStatus,
            sensitivity = payload.sensitivity,
        )

        return TransferResult(
            delivered = true,
            message = "Anonymized report delivered to ${packet.targetAlias} by ${packet.transport.label()}.",
            packet = packet,
            receivedItem = item,
        )
    }

    private fun sealPayload(payload: SanitizedReportPayload): String {
        val clearText = listOf(
            payload.reportType,
            payload.generalArea,
            payload.timeWindow,
            payload.submittedAtEpochSeconds.toString(),
            payload.observedRisk,
            payload.verificationStatus.name,
            payload.sensitivity.name,
        ).joinToString("\u001F")
        val iv = ByteArray(IvByteSize)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(AesGcmTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, demoKey(), GCMParameterSpec(GcmTagBits, iv))
        val cipherText = cipher.doFinal(clearText.toByteArray())
        return Base64.getEncoder().encodeToString(iv + cipherText)
    }

    private fun unsealPayload(sealedPayload: String): SanitizedReportPayload {
        val sealedBytes = Base64.getDecoder().decode(sealedPayload)
        val iv = sealedBytes.copyOfRange(0, IvByteSize)
        val cipherText = sealedBytes.copyOfRange(IvByteSize, sealedBytes.size)
        val cipher = Cipher.getInstance(AesGcmTransformation)
        cipher.init(Cipher.DECRYPT_MODE, demoKey(), GCMParameterSpec(GcmTagBits, iv))
        val parts = String(cipher.doFinal(cipherText)).split("\u001F")
        return SanitizedReportPayload(
            reportType = parts.getOrElse(0) { "unknown report" },
            generalArea = parts.getOrElse(1) { "general area withheld" },
            timeWindow = parts.getOrElse(2) { "time window not specified" },
            submittedAtEpochSeconds = parts.getOrNull(3)?.toLongOrNull() ?: 0L,
            observedRisk = parts.getOrElse(4) { "details withheld" },
            verificationStatus = parts.getOrNull(5)?.let { VerificationStatus.valueOf(it) } ?: VerificationStatus.PendingVerification,
            sensitivity = parts.getOrNull(6)?.let { ReportSensitivity.valueOf(it) } ?: ReportSensitivity.Community,
        )
    }

    private fun demoKey(): SecretKeySpec {
        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest("jirani-demo-nearby-transfer-key".toByteArray())
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun SyncTransport.label(): String = when (this) {
        SyncTransport.NearbyConnections -> "Nearby Connections"
        SyncTransport.WifiDirect -> "Wi-Fi Direct"
        SyncTransport.AndroidShareSheet -> "Android Sharesheet"
        SyncTransport.QrOrEncryptedFile -> "QR/encrypted file"
        SyncTransport.RemoteRustGateway -> "Rust gateway"
    }
}

object ReportingSyncPolicy {
    fun createRecord(
        reportType: String,
        generalLocation: String,
        details: String,
    ): SafetyReportRecord {
        val now = Instant.now().epochSecond
        val sensitivity = classifySensitivity(reportType, details)
        return SafetyReportRecord(
            recordId = "report-${stableHash("$reportType|$generalLocation|$details|$now").take(12)}",
            reportType = reportType.ifBlank { "Unclassified" },
            generalLocation = sanitizeLocation(generalLocation),
            timeWindow = extractTimeWindow(details),
            incidentSummary = sanitizeSummary(details),
            sensitivity = sensitivity,
            expiresAtEpochSeconds = now + retentionSeconds(sensitivity),
        )
    }

    fun createEnvelope(record: SafetyReportRecord): SyncEnvelope {
        val payload = SanitizedReportPayload(
            reportType = record.reportType,
            generalArea = record.generalLocation,
            timeWindow = record.timeWindow,
            submittedAtEpochSeconds = record.createdAtEpochSeconds,
            observedRisk = record.incidentSummary,
            verificationStatus = record.verificationStatus,
            sensitivity = record.sensitivity,
        )
        return SyncEnvelope(
            envelopeId = "env-${stableHash("${record.recordId}|${payloadHash(payload)}").take(12)}",
            recordType = "SafetyReportRecord",
            recordId = record.recordId,
            contentHash = payloadHash(payload),
            version = 1,
            lastModifiedBucket = "day-${record.createdAtEpochSeconds / 86_400}",
            audienceTier = audienceTier(record.sensitivity),
            syncState = syncState(record.sensitivity),
            allowedTransports = allowedTransports(record.sensitivity),
            payload = payload,
            expiresAtEpochSeconds = record.expiresAtEpochSeconds,
        )
    }

    fun payloadHash(payload: SanitizedReportPayload): String =
        stableHash(payloadHashContent(payload))

    private fun classifySensitivity(reportType: String, details: String): ReportSensitivity {
        val text = "$reportType $details".lowercase()
        return when {
            text.containsAny("gbv", "gender based", "sexual", "rape", "defilement", "domestic", "husband", "wife", "partner abuse") ->
                ReportSensitivity.SurvivorCentered
            text.containsAny("armed", "gun", "machete", "attack", "killed", "burned", "retaliation", "revenge", "violence") ->
                ReportSensitivity.Protection
            else -> ReportSensitivity.Community
        }
    }

    private fun audienceTier(sensitivity: ReportSensitivity): SyncAudienceTier = when (sensitivity) {
        ReportSensitivity.SurvivorCentered -> SyncAudienceTier.SurvivorSupportOnly
        ReportSensitivity.Protection -> SyncAudienceTier.ProtectionActors
        ReportSensitivity.Community -> SyncAudienceTier.TrustedVerifier
    }

    private fun syncState(sensitivity: ReportSensitivity): SyncState = when (sensitivity) {
        ReportSensitivity.SurvivorCentered -> SyncState.LocalHold
        ReportSensitivity.Protection -> SyncState.WaitingForTrustedPeer
        ReportSensitivity.Community -> SyncState.ReadyForNearbyShare
    }

    private fun allowedTransports(sensitivity: ReportSensitivity): List<SyncTransport> = when (sensitivity) {
        ReportSensitivity.SurvivorCentered -> listOf(SyncTransport.QrOrEncryptedFile, SyncTransport.AndroidShareSheet)
        ReportSensitivity.Protection -> listOf(
            SyncTransport.NearbyConnections,
            SyncTransport.WifiDirect,
            SyncTransport.AndroidShareSheet,
            SyncTransport.RemoteRustGateway,
        )
        ReportSensitivity.Community -> SyncTransport.entries
    }

    private fun retentionSeconds(sensitivity: ReportSensitivity): Long = when (sensitivity) {
        ReportSensitivity.SurvivorCentered -> 7L * 24L * 60L * 60L
        ReportSensitivity.Protection -> 14L * 24L * 60L * 60L
        ReportSensitivity.Community -> 30L * 24L * 60L * 60L
    }

    private fun payloadHashContent(payload: SanitizedReportPayload): String =
        listOf(
            payload.reportType,
            payload.generalArea,
            payload.timeWindow,
            payload.submittedAtEpochSeconds.toString(),
            payload.observedRisk,
            payload.verificationStatus.name,
            payload.sensitivity.name,
        ).joinToString("|")

    private fun sanitizeLocation(location: String): String =
        location
            .replace(Regex("""\b\d{2,}\b"""), "nearby area")
            .replace(Regex("""(?i)\b(home|house|plot|room)\s+\S+"""), "private residence")
            .trim()
            .ifBlank { "general area withheld" }

    private fun sanitizeSummary(details: String): String =
        details
            .replace(Regex("""\+?\d[\d\s-]{6,}\d"""), "[phone removed]")
            .replace(Regex("""(?i)\b[A-Z][a-z]+\s+[A-Z][a-z]+\b"""), "[name removed]")
            .trim()
            .ifBlank { "Details withheld until trusted review." }

    private fun extractTimeWindow(details: String): String {
        val text = details.lowercase()
        return when {
            text.contains("morning") -> "morning"
            text.contains("afternoon") -> "afternoon"
            text.contains("evening") -> "evening"
            text.contains("night") -> "night"
            text.contains("today") -> "today"
            text.contains("yesterday") -> "yesterday"
            else -> "time window not specified"
        }
    }

    internal fun stableHash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { contains(it) }
}
