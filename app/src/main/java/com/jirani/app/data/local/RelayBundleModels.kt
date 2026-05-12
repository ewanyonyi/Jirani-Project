package com.jirani.app.data.local

import java.time.Instant

data class RelayPublicHeader(
    val alertType: String,
    val generalArea: String,
    val timeWindow: String,
    val riskLevel: String,
    val message: String,
    val verificationStatus: VerificationStatus,
    val audienceTier: SyncAudienceTier,
    val sensitivity: ReportSensitivity,
)

data class RelayBundle(
    val bundleId: String,
    val publicHeader: RelayPublicHeader,
    val encryptedPayload: String,
    val payloadHash: String,
    val bundleHash: String,
    val expiresAtEpochSeconds: Long,
)

object RelayBundlePolicy {
    fun createBundle(
        publicHeader: RelayPublicHeader,
        encryptedPayload: String,
        expiresAtEpochSeconds: Long,
    ): RelayBundle {
        val payloadHash = payloadHash(encryptedPayload)
        val bundleHash = bundleHash(publicHeader, payloadHash)
        return RelayBundle(
            bundleId = "bundle-${bundleHash.take(12)}",
            publicHeader = publicHeader,
            encryptedPayload = encryptedPayload,
            payloadHash = payloadHash,
            bundleHash = bundleHash,
            expiresAtEpochSeconds = expiresAtEpochSeconds,
        )
    }

    fun publicHeaderFromEnvelope(envelope: SyncEnvelope): RelayPublicHeader =
        RelayPublicHeader(
            alertType = envelope.payload.reportType,
            generalArea = envelope.payload.generalArea,
            timeWindow = envelope.payload.timeWindow,
            riskLevel = riskLevel(envelope.payload.sensitivity),
            message = envelope.payload.observedRisk,
            verificationStatus = envelope.payload.verificationStatus,
            audienceTier = envelope.audienceTier,
            sensitivity = envelope.payload.sensitivity,
        )

    fun validateForRelay(
        bundle: RelayBundle,
        nowEpochSeconds: Long = Instant.now().epochSecond,
    ): String? = when {
        nowEpochSeconds >= bundle.expiresAtEpochSeconds -> "Relay bundle is expired."
        bundle.encryptedPayload.isBlank() -> "Relay bundle is missing encrypted payload."
        bundle.publicHeader.sensitivity == ReportSensitivity.SurvivorCentered ->
            "Survivor-centered reports stay outside broad relay."
        bundle.publicHeader.audienceTier == SyncAudienceTier.SurvivorSupportOnly ->
            "Survivor-support reports require explicit private handoff."
        containsUnsafePublicHeaderText(bundle.publicHeader) ->
            "Relay public header contains identifying or exact-home details."
        payloadHash(bundle.encryptedPayload) != bundle.payloadHash ->
            "Relay payload hash does not match encrypted payload."
        bundleHash(bundle.publicHeader, bundle.payloadHash) != bundle.bundleHash ->
            "Relay bundle hash does not match public header and payload hash."
        else -> null
    }

    fun payloadHash(encryptedPayload: String): String =
        ReportingSyncPolicy.stableHash(encryptedPayload)

    fun bundleHash(publicHeader: RelayPublicHeader, payloadHash: String): String =
        ReportingSyncPolicy.stableHash(bundleHashContent(publicHeader, payloadHash))

    fun bundleHashContent(publicHeader: RelayPublicHeader, payloadHash: String): String =
        listOf(
            publicHeader.alertType,
            publicHeader.generalArea,
            publicHeader.timeWindow,
            publicHeader.riskLevel,
            publicHeader.message,
            publicHeader.verificationStatus.name,
            publicHeader.audienceTier.name,
            publicHeader.sensitivity.name,
            payloadHash,
        ).joinToString("|")

    private fun riskLevel(sensitivity: ReportSensitivity): String = when (sensitivity) {
        ReportSensitivity.Protection -> "Elevated"
        ReportSensitivity.Community -> "Reported"
        ReportSensitivity.SurvivorCentered -> "Private"
    }

    private fun containsUnsafePublicHeaderText(publicHeader: RelayPublicHeader): Boolean =
        listOf(
            publicHeader.alertType,
            publicHeader.generalArea,
            publicHeader.timeWindow,
            publicHeader.riskLevel,
            publicHeader.message,
        ).any { text -> text.containsPiiOrExactHomeHint() }

    private fun String.containsPiiOrExactHomeHint(): Boolean =
        PhonePattern.containsMatchIn(this) ||
            ExactCoordinatePattern.containsMatchIn(this) ||
            ExactHomePattern.containsMatchIn(this) ||
            HouseholdIdentifierPattern.containsMatchIn(this)

    private val PhonePattern = Regex("""\+?\d[\d\s-]{6,}\d""")
    private val ExactCoordinatePattern = Regex("""-?\d{1,2}\.\d{4,}\s*,\s*-?\d{1,3}\.\d{4,}""")
    private val ExactHomePattern = Regex("""(?i)\b(home|house|plot|room)\s+\S+""")
    private val HouseholdIdentifierPattern = Regex("""\b[A-Z][a-z]+\s+[A-Z][a-z]+\b""")
}
