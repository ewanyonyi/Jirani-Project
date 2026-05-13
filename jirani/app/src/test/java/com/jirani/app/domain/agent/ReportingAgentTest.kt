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

        assertEquals("livestock or grazing dispute", guidance.threatType)
        assertTrue(guidance.safeNextSteps.any { it.contains("Do not collect names") })
        assertFalse(guidance.missingNonIdentifyingDetails.contains("general location"))
        assertTrue(guidance.mediationReadiness.contains("Possible after review"))
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

    @Test
    fun process_whenViolenceIsActive_blocksMediation() {
        val guidance = agent.process(
            SafetyReportRequest("People were attacked and shops burned near the market at night."),
        )

        assertEquals("active violence or retaliation risk", guidance.threatType)
        assertTrue(guidance.triageOutcome.contains("Protection first"))
        assertTrue(guidance.mediationReadiness.contains("Not ready"))
    }

    @Test
    fun process_whenGbvReport_prioritizesSurvivorSafetyAndBlocksMediation() {
        val guidance = agent.process(
            SafetyReportRequest("GBV. A survivor needs private help after sexual violence at night."),
        )

        assertEquals("GBV survivor safety report", guidance.threatType)
        assertTrue(guidance.triageOutcome.contains("Survivor safety first"))
        assertTrue(guidance.mediationReadiness.contains("Not for mediation"))
        assertTrue(guidance.safeNextSteps.any { it.contains("survivor consent") })
    }

    @Test
    fun process_whenDomesticViolenceReport_usesPrivateSupportPath() {
        val guidance = agent.process(
            SafetyReportRequest("Domestic. Partner abuse at home this evening."),
        )

        assertEquals("domestic violence safety report", guidance.threatType)
        assertTrue(guidance.localActorsToNotify.any { it.contains("survivor-chosen") })
        assertTrue(guidance.safeNextSteps.any { it.contains("do not create a community alert") })
    }
}
