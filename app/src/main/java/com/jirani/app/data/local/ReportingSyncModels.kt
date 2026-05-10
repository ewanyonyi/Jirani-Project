package com.jirani.app.data.local

import java.security.MessageDigest
import java.time.Instant

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
)

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
            observedRisk = record.incidentSummary,
            verificationStatus = record.verificationStatus,
            sensitivity = record.sensitivity,
        )
        val content = listOf(
            payload.reportType,
            payload.generalArea,
            payload.timeWindow,
            payload.observedRisk,
            payload.verificationStatus.name,
            payload.sensitivity.name,
        ).joinToString("|")

        return SyncEnvelope(
            envelopeId = "env-${stableHash("${record.recordId}|$content").take(12)}",
            recordType = "SafetyReportRecord",
            recordId = record.recordId,
            contentHash = stableHash(content),
            version = 1,
            lastModifiedBucket = "day-${record.createdAtEpochSeconds / 86_400}",
            audienceTier = audienceTier(record.sensitivity),
            syncState = syncState(record.sensitivity),
            allowedTransports = allowedTransports(record.sensitivity),
            payload = payload,
        )
    }

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
        ReportSensitivity.Protection -> listOf(SyncTransport.NearbyConnections, SyncTransport.WifiDirect, SyncTransport.AndroidShareSheet)
        ReportSensitivity.Community -> SyncTransport.entries
    }

    private fun retentionSeconds(sensitivity: ReportSensitivity): Long = when (sensitivity) {
        ReportSensitivity.SurvivorCentered -> 7L * 24L * 60L * 60L
        ReportSensitivity.Protection -> 14L * 24L * 60L * 60L
        ReportSensitivity.Community -> 30L * 24L * 60L * 60L
    }

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

    private fun stableHash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { contains(it) }
}
