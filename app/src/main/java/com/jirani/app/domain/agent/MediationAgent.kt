package com.jirani.app.domain.agent

data class MediationRequest(
    val description: String,
)

data class MediationGuidance(
    val summary: String,
    val concerns: List<String>,
    val recommendations: List<String>,
    val nextStep: String,
    val safetyNote: String?,
)

class MediationAgent : JiraniAgent<MediationRequest, MediationGuidance> {
    override val name: String = "Mediation Agent"

    override fun process(input: MediationRequest): MediationGuidance {
        val description = input.description.trim()
        if (description.isBlank()) {
            return MediationGuidance(
                summary = "No dispute details have been provided yet.",
                concerns = listOf("unclear needs", "missing facts"),
                recommendations = listOf(
                    "Capture the issue using neutral words.",
                    "Describe what happened, where it happened, and what outcome would feel fair.",
                ),
                nextStep = "Add a short description before creating an agreement record.",
                safetyNote = null,
            )
        }

        val lower = description.lowercase()
        val concerns = buildConcerns(lower)
        val safetyNote = when {
            hasSafetyRisk(lower) -> "Prioritize immediate safety and involve trusted local leaders before direct confrontation."
            else -> "Verify key facts with trusted community members before escalation."
        }

        return MediationGuidance(
            summary = "A community concern needs calm discussion and neutral documentation.",
            concerns = concerns,
            recommendations = buildRecommendations(lower),
            nextStep = "Record any shared commitments with anonymous labels such as Party A and Party B.",
            safetyNote = safetyNote,
        )
    }

    private fun buildConcerns(text: String): List<String> {
        val concerns = mutableListOf<String>()
        if (text.containsAny("water", "well", "river", "pump")) concerns += "shared water access"
        if (text.containsAny("land", "field", "grazing", "boundary")) concerns += "land or grazing access"
        if (text.containsAny("threat", "armed", "machete", "attack", "violence")) concerns += "safety risk"
        if (text.containsAny("rumor", "heard", "claim")) concerns += "unverified information"
        if (concerns.isEmpty()) concerns += "trust, fairness, and shared expectations"
        return concerns
    }

    private fun buildRecommendations(text: String): List<String> {
        val recommendations = mutableListOf(
            "Restate the concern without blame and ask each side what they need most urgently.",
            "Separate confirmed facts from assumptions before deciding on action.",
        )

        if (text.containsAny("water", "well", "river", "pump")) {
            recommendations += "Discuss a temporary access schedule while the community verifies the facts."
        } else if (text.containsAny("land", "field", "grazing", "boundary")) {
            recommendations += "Map the disputed access point and agree on a short review period."
        } else {
            recommendations += "Invite a trusted mediator to help document practical next steps."
        }

        return recommendations
    }

    private fun hasSafetyRisk(text: String): Boolean =
        text.containsAny("threat", "armed", "machete", "attack", "violence", "weapon")

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { contains(it) }
}
