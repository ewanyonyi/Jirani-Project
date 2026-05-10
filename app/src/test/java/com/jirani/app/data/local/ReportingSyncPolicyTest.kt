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
}
