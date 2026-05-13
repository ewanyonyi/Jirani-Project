package com.jirani.app.data.local

import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import java.security.KeyFactory
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

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

data class RelayBundleCarrierItem(
    val bundle: RelayBundle,
    val deliveredDeviceAliases: List<String> = emptyList(),
    val sendingDeviceAliases: List<String> = emptyList(),
)

data class RelayBundleInboxItem(
    val bundle: RelayBundle,
    val receivedFromAliases: List<String> = emptyList(),
) {
    val peerCount: Int = receivedFromAliases.distinct().size
    val verificationLabel: String =
        if (peerCount >= 2) "Corroborated by nearby peers" else "Pending local verification"
}

object RelayBundlePolicy {
    private const val AesGcmTransformation = "AES/GCM/NoPadding"
    private const val RsaOaepTransformation = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private const val GcmTagBits = 128
    private const val IvByteSize = 12
    @Volatile
    private var gatewayPublicKey: PublicKey? = null

    fun configureGatewayPublicKey(encodedPublicKey: String?): Boolean {
        val keyText = encodedPublicKey?.trim().orEmpty()
        if (keyText.isBlank()) {
            gatewayPublicKey = null
            return false
        }
        val body = keyText
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val publicKey = runCatching {
            val bytes = Base64.getDecoder().decode(body)
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes))
        }.getOrNull() ?: return false
        gatewayPublicKey = publicKey
        return true
    }

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

    fun createBundleFromEnvelope(envelope: SyncEnvelope): RelayBundle =
        createBundle(
            publicHeader = publicHeaderFromEnvelope(envelope),
            encryptedPayload = encryptPayload(envelope.payload),
            expiresAtEpochSeconds = envelope.expiresAtEpochSeconds,
        )

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

    private fun encryptPayload(payload: SanitizedReportPayload): String {
        val clearText = listOf(
            payload.reportType,
            payload.generalArea,
            payload.timeWindow,
            payload.submittedAtEpochSeconds.toString(),
            payload.observedRisk,
            payload.verificationStatus.name,
            payload.sensitivity.name,
        ).joinToString("\u001F")
        gatewayPublicKey?.let { publicKey -> return encryptPayloadWithGatewayKey(clearText, publicKey) }

        val iv = ByteArray(IvByteSize)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(AesGcmTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, demoRelayKey(), GCMParameterSpec(GcmTagBits, iv))
        val cipherText = cipher.doFinal(clearText.toByteArray())
        return Base64.getEncoder().encodeToString(iv + cipherText)
    }

    private fun demoRelayKey(): SecretKeySpec {
        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest("jirani-demo-relay-bundle-key".toByteArray())
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun encryptPayloadWithGatewayKey(clearText: String, publicKey: PublicKey): String {
        val aesKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val iv = ByteArray(IvByteSize)
        SecureRandom().nextBytes(iv)
        val payloadCipher = Cipher.getInstance(AesGcmTransformation)
        payloadCipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(GcmTagBits, iv))
        val cipherText = payloadCipher.doFinal(clearText.toByteArray())

        val keyCipher = Cipher.getInstance(RsaOaepTransformation)
        keyCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val wrappedKey = keyCipher.doFinal(aesKey.encoded)

        return listOf(
            "\"alg\":\"RSA-OAEP-SHA256+A256GCM\"",
            "\"wrappedKey\":\"${Base64.getEncoder().encodeToString(wrappedKey)}\"",
            "\"iv\":\"${Base64.getEncoder().encodeToString(iv)}\"",
            "\"ciphertext\":\"${Base64.getEncoder().encodeToString(cipherText)}\"",
        ).joinToString(prefix = "{", postfix = "}")
    }

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
