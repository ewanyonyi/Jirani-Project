package com.jirani.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportingSyncPolicyTest {
    @Test
    fun createEnvelope_forGbvReport_usesSurvivorSupportOnlyAudience() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "GBV survivor safety report",
            generalLocation = "near market house 42",
            details = "Survivor reported sexual violence at night. Call 0712 333 444.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)

        assertEquals(ReportSensitivity.SurvivorCentered, record.sensitivity)
        assertEquals(SyncAudienceTier.SurvivorSupportOnly, envelope.audienceTier)
        assertEquals(SyncState.LocalHold, envelope.syncState)
        assertFalse(envelope.allowedTransports.contains(SyncTransport.NearbyConnections))
        assertFalse(envelope.payload.observedRisk.contains("0712 333 444"))
    }

    @Test
    fun createEnvelope_forCommunityReport_canUseNearbySharing() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "livestock or grazing dispute",
            generalLocation = "near the river",
            details = "Cattle crossed the grazing boundary this morning.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)

        assertEquals(ReportSensitivity.Community, record.sensitivity)
        assertEquals(SyncAudienceTier.TrustedVerifier, envelope.audienceTier)
        assertEquals(SyncState.ReadyForNearbyShare, envelope.syncState)
        assertTrue(envelope.allowedTransports.contains(SyncTransport.NearbyConnections))
    }

    @Test
    fun sendToDevice_forCommunityReport_deliversOnlySanitizedPayload() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "livestock or grazing dispute",
            generalLocation = "near market house 19",
            details = "John Kamau saw cattle cross the river this morning. Call 0712 333 444.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)
        val target = NearbyJiraniDevice(
            deviceAlias = "Elder verifier phone",
            trusted = true,
            supportedTransports = listOf(SyncTransport.NearbyConnections),
        )

        val result = ReportingDeviceTransfer.sendToDevice(envelope, target)
        val received = result.receivedItem!!

        assertTrue(result.delivered)
        assertEquals("livestock or grazing dispute", received.reportType)
        assertFalse(received.generalArea.contains("19"))
        assertFalse(received.observedRisk.contains("John Kamau"))
        assertFalse(received.observedRisk.contains("0712 333 444"))
        assertEquals(envelope.contentHash, result.packet?.contentHash)
        assertFalse(result.packet?.sealedPayload.orEmpty().contains("cattle"))
    }

    @Test
    fun sendToDevice_forGbvReport_doesNotUseNearbyBroadcast() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "GBV survivor safety report",
            generalLocation = "near clinic",
            details = "Survivor needs private support after sexual violence at night.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)
        val target = NearbyJiraniDevice(
            deviceAlias = "Elder verifier phone",
            trusted = true,
            supportedTransports = listOf(SyncTransport.NearbyConnections),
        )

        val result = ReportingDeviceTransfer.sendToDevice(envelope, target)

        assertFalse(result.delivered)
        assertTrue(result.message.contains("private handoff"))
    }

    @Test
    fun receivePacket_whenHashIsChanged_rejectsPacket() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "livestock or grazing dispute",
            generalLocation = "near river",
            details = "Cattle crossed the grazing boundary this morning.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)
        val target = NearbyJiraniDevice(
            deviceAlias = "Elder verifier phone",
            trusted = true,
            supportedTransports = listOf(SyncTransport.NearbyConnections),
        )
        val packet = ReportingDeviceTransfer.sendToDevice(envelope, target).packet!!

        val result = ReportingDeviceTransfer.receivePacket(
            packet = packet.copy(contentHash = "tampered"),
            fromAlias = "Unknown device",
        )

        assertFalse(result.delivered)
        assertTrue(result.message.contains("integrity"))
    }

    @Test
    fun sendToDevices_sendsToMaximumFiveUniqueDevices() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "livestock or grazing dispute",
            generalLocation = "near river",
            details = "Cattle crossed the grazing boundary this morning.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)
        val devices = (1..7).map { index ->
            NearbyJiraniDevice(
                deviceAlias = "Verifier $index",
                trusted = true,
                supportedTransports = listOf(SyncTransport.NearbyConnections),
            )
        }

        val result = ReportingDeviceTransfer.sendToDevices(envelope, devices)

        assertEquals(5, result.deliveredCount)
        assertEquals(5, result.updatedEnvelope.deliveredDeviceAliases.distinct().size)
        assertTrue(result.message.contains("Relay limit reached"))
    }

    @Test
    fun sendToDevices_whenReportIsStale_doesNotSend() {
        val record = ReportingSyncPolicy.createRecord(
            reportType = "livestock or grazing dispute",
            generalLocation = "near river",
            details = "Cattle crossed the grazing boundary this morning.",
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record).copy(expiresAtEpochSeconds = 10)
        val devices = listOf(
            NearbyJiraniDevice(
                deviceAlias = "Verifier 1",
                trusted = true,
                supportedTransports = listOf(SyncTransport.NearbyConnections),
            ),
        )

        val result = ReportingDeviceTransfer.sendToDevices(
            envelope = envelope,
            targets = devices,
            nowEpochSeconds = 11,
        )

        assertEquals(0, result.deliveredCount)
        assertTrue(result.message.contains("stale"))
    }
}
