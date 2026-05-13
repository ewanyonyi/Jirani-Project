package com.jirani.app.sync

import com.jirani.app.data.local.RelayBundle
import com.jirani.app.data.local.RelayPublicHeader
import com.jirani.app.data.local.ReportSensitivity
import com.jirani.app.data.local.SyncAudienceTier
import com.jirani.app.data.local.VerificationStatus
import org.json.JSONObject

object RelayBundleJsonCodec {
    fun encode(bundle: RelayBundle): JSONObject =
        JSONObject()
            .put("bundleId", bundle.bundleId)
            .put("publicHeader", JSONObject()
                .put("alertType", bundle.publicHeader.alertType)
                .put("generalArea", bundle.publicHeader.generalArea)
                .put("timeWindow", bundle.publicHeader.timeWindow)
                .put("riskLevel", bundle.publicHeader.riskLevel)
                .put("message", bundle.publicHeader.message)
                .put("verificationStatus", bundle.publicHeader.verificationStatus.name)
                .put("audienceTier", bundle.publicHeader.audienceTier.name)
                .put("sensitivity", bundle.publicHeader.sensitivity.name))
            .put("encryptedPayload", bundle.encryptedPayload)
            .put("payloadHash", bundle.payloadHash)
            .put("bundleHash", bundle.bundleHash)
            .put("expiresAtEpochSeconds", bundle.expiresAtEpochSeconds)

    fun decode(json: JSONObject): RelayBundle? =
        runCatching {
            val publicHeader = json.getJSONObject("publicHeader").let { header ->
                RelayPublicHeader(
                    alertType = header.optString("alertType"),
                    generalArea = header.optString("generalArea", "general area withheld"),
                    timeWindow = header.optString("timeWindow", "time window not specified"),
                    riskLevel = header.optString("riskLevel", "Reported"),
                    message = header.optString("message"),
                    verificationStatus = header.optEnum("verificationStatus", VerificationStatus.PendingVerification),
                    audienceTier = header.optEnum("audienceTier", SyncAudienceTier.TrustedVerifier),
                    sensitivity = header.optEnum("sensitivity", ReportSensitivity.Community),
                )
            }
            RelayBundle(
                bundleId = json.optString("bundleId"),
                publicHeader = publicHeader,
                encryptedPayload = json.optString("encryptedPayload"),
                payloadHash = json.optString("payloadHash"),
                bundleHash = json.optString("bundleHash"),
                expiresAtEpochSeconds = json.optLong("expiresAtEpochSeconds"),
            )
        }.getOrNull()

    private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T =
        runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(fallback)
}
