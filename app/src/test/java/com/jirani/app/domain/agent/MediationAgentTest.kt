package com.jirani.app.domain.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediationAgentTest {
    private val agent = MediationAgent()

    @Test
    fun process_whenDescriptionMentionsWater_includesWaterConcern() {
        val guidance = agent.process(
            MediationRequest("Our neighbors blocked access to the shared well."),
        )

        assertTrue(guidance.concerns.contains("shared water access"))
        assertTrue(guidance.recommendations.any { it.contains("temporary access schedule") })
    }

    @Test
    fun process_whenDescriptionIsBlank_returnsMissingDetailsGuidance() {
        val guidance = agent.process(MediationRequest(" "))

        assertEquals("No dispute details have been provided yet.", guidance.summary)
        assertTrue(guidance.concerns.contains("missing facts"))
    }

    @Test
    fun process_whenSafetyRiskAppears_prioritizesSafety() {
        val guidance = agent.process(
            MediationRequest("There was a threat of violence near the market path."),
        )

        assertTrue(guidance.concerns.contains("safety risk"))
        assertTrue(guidance.safetyNote?.contains("Prioritize immediate safety") == true)
    }
}
