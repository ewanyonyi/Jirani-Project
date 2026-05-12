package com.jirani.app.data.local

import java.security.KeyPairGenerator
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelayBundlePolicyTest {
    @Test
    fun createBundle_usesDeterministicHashContract() {
        val header = RelayPublicHeader(
            alertType = "ResourceDispute",
            generalArea = "near river",
            timeWindow = "morning",
            riskLevel = "Elevated",
            message = "Cattle movement reported near shared grazing boundary.",
            verificationStatus = VerificationStatus.PendingVerification,
            audienceTier = SyncAudienceTier.TrustedVerifier,
            sensitivity = ReportSensitivity.Community,
        )

        val bundle = RelayBundlePolicy.createBundle(
            publicHeader = header,
            encryptedPayload = "base64-encoded-ciphertext",
            expiresAtEpochSeconds = 1_900_000_000,
        )

        assertEquals(
            "ResourceDispute|near river|morning|Elevated|Cattle movement reported near shared grazing boundary.|PendingVerification|TrustedVerifier|Community|56be8e036dc43103ab211b63dafd8ab0ef3ed1ef3d6badca734850549b00685a",
            RelayBundlePolicy.bundleHashContent(header, bundle.payloadHash),
        )
        assertEquals(
            "56be8e036dc43103ab211b63dafd8ab0ef3ed1ef3d6badca734850549b00685a",
            bundle.payloadHash,
        )
        assertEquals(
            "c537927be2040e8ef0252d04a8c1617d308f46423afaebc2d171ad3188af1e39",
            bundle.bundleHash,
        )
        assertEquals("bundle-c537927be204", bundle.bundleId)
        assertNull(RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = 1_800_000_000))
    }

    @Test
    fun validateForRelay_rejectsSurvivorCenteredBundles() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "GBV survivor safety report",
            generalLocation = "near clinic",
            details = "Survivor needs private support after sexual violence at night.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)
        val bundle = RelayBundlePolicy.createBundle(
            publicHeader = RelayBundlePolicy.publicHeaderFromEnvelope(envelope),
            encryptedPayload = "private-ciphertext",
            expiresAtEpochSeconds = envelope.expiresAtEpochSeconds,
        )

        val reason = RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = record.createdAtEpochSeconds)

        assertTrue(reason.orEmpty().contains("Survivor-centered"))
    }

    @Test
    fun validateForRelay_rejectsPublicHeaderPiiAndExactHomeHints() {
        val header = RelayPublicHeader(
            alertType = "ResourceDispute",
            generalArea = "near house 42",
            timeWindow = "morning",
            riskLevel = "Reported",
            message = "Call 0712 333 444 after John Kamau arrives.",
            verificationStatus = VerificationStatus.PendingVerification,
            audienceTier = SyncAudienceTier.TrustedVerifier,
            sensitivity = ReportSensitivity.Community,
        )
        val bundle = RelayBundlePolicy.createBundle(
            publicHeader = header,
            encryptedPayload = "ciphertext",
            expiresAtEpochSeconds = 1_900_000_000,
        )

        val reason = RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = 1_800_000_000)

        assertTrue(reason.orEmpty().contains("public header"))
    }

    @Test
    fun validateForRelay_rejectsTamperedPayloadHash() {
        val header = RelayPublicHeader(
            alertType = "ResourceDispute",
            generalArea = "near river",
            timeWindow = "morning",
            riskLevel = "Reported",
            message = "Cattle movement reported near shared grazing boundary.",
            verificationStatus = VerificationStatus.PendingVerification,
            audienceTier = SyncAudienceTier.TrustedVerifier,
            sensitivity = ReportSensitivity.Community,
        )
        val bundle = RelayBundlePolicy.createBundle(
            publicHeader = header,
            encryptedPayload = "ciphertext",
            expiresAtEpochSeconds = 1_900_000_000,
        ).copy(payloadHash = "tampered")

        val reason = RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = 1_800_000_000)

        assertTrue(reason.orEmpty().contains("payload hash"))
    }

    @Test
    fun createBundleFromEnvelope_buildsValidCommunityRelayBundle() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "livestock or grazing dispute",
            generalLocation = "near the river",
            details = "Cattle crossed the grazing boundary this morning.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)

        val bundle = RelayBundlePolicy.createBundleFromEnvelope(envelope)

        assertEquals(envelope.payload.reportType, bundle.publicHeader.alertType)
        assertEquals(envelope.payload.generalArea, bundle.publicHeader.generalArea)
        assertEquals(envelope.payload.observedRisk, bundle.publicHeader.message)
        assertTrue(bundle.encryptedPayload.isNotBlank())
        assertNull(RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = record.createdAtEpochSeconds))
    }

    @Test
    fun createBundleFromEnvelope_keepsSurvivorCenteredBundleInvalidForBroadRelay() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "domestic violence report",
            generalLocation = "near clinic",
            details = "Wife needs private survivor support tonight.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)

        val bundle = RelayBundlePolicy.createBundleFromEnvelope(envelope)
        val reason = RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = record.createdAtEpochSeconds)

        assertTrue(reason.orEmpty().contains("Survivor-centered"))
    }

    @Test
    fun createBundleFromEnvelope_usesConfiguredGatewayPublicKeyForPayloadEncryption() {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        assertTrue(RelayBundlePolicy.configureGatewayPublicKey(publicKey))
        try {
            val record = ReportingSyncPolicy.createRecord(
                reportType = "livestock or grazing dispute",
                generalLocation = "near the river",
                details = "Cattle crossed the grazing boundary this morning.",
            )
            val envelope = ReportingSyncPolicy.createEnvelope(record)

            val bundle = RelayBundlePolicy.createBundleFromEnvelope(envelope)

            assertTrue(bundle.encryptedPayload.contains("RSA-OAEP-SHA256+A256GCM"))
            assertNull(RelayBundlePolicy.validateForRelay(bundle, nowEpochSeconds = record.createdAtEpochSeconds))
        } finally {
            RelayBundlePolicy.configureGatewayPublicKey(null)
        }
    }
}
