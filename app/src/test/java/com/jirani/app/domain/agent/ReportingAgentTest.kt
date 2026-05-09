package com.jirani.app.domain.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportingAgentTest {
    private val agent = ReportingAgent()

    @Test
    fun process_classifiesRustlingWithoutRequestingPersonalIdentifiers() {
        val guidance = agent.process(
            SafetyReportRequest("Cattle rustlers were seen near the river this morning."),
        )

        assertEquals("cattle rustling risk", guidance.threatType)
        assertTrue(guidance.safeNextSteps.any { it.contains("Avoid collecting names") })
        assertFalse(guidance.missingNonIdentifyingDetails.contains("general location"))
    }

    @Test
    fun process_whenReportIsBlank_requestsOnlyNonIdentifyingDetails() {
        val guidance = agent.process(SafetyReportRequest(""))

        assertEquals("unclassified", guidance.threatType)
        assertEquals(
            listOf("general location", "time window", "observed risk"),
            guidance.missingNonIdentifyingDetails,
        )
    }
}
