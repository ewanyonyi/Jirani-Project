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
            hasSafetyRisk(lower) -> "Mediation is not ready while threats, weapons, retaliation, or attacks are active."
            else -> "Mediation should be opened by trusted elders, a peace committee, OSF/community partner, or another accepted neutral actor after local review."
        }

        return MediationGuidance(
            summary = "A community concern needs calm discussion and neutral documentation.",
            concerns = concerns,
            recommendations = buildRecommendations(lower),
            nextStep = buildNextStep(lower),
            safetyNote = safetyNote,
        )
    }

    private fun buildConcerns(text: String): List<String> {
        val concerns = mutableListOf<String>()
        if (text.containsAny("water", "well", "river", "pump")) concerns += "shared water access"
        if (text.containsAny("land", "field", "grazing", "boundary")) concerns += "land or grazing access"
        if (text.containsAny("threat", "armed", "machete", "gun", "attack", "violence", "retaliation", "revenge", "killed")) concerns += "safety risk"
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
            recommendations += "Ask elders from both sides to confirm the water point, users, and temporary access hours before any agreement is written."
        } else if (text.containsAny("land", "field", "grazing", "boundary")) {
            recommendations += "Let elders or grazing committee members mark the disputed route or boundary and agree on a short review period."
        } else if (hasSafetyRisk(text)) {
            recommendations += "Pause mediation and route the case back to safety reporting until trusted actors say people can meet safely."
        } else {
            recommendations += "Invite accepted elders, a religious leader, a peace committee member, or OSF/community partner to help document practical next steps."
        }

        return recommendations
    }

    private fun buildNextStep(text: String): String =
        if (hasSafetyRisk(text)) {
            "Keep this as a safety report until local protection and verification are complete."
        } else {
            "If both sides accept the process, record commitments with anonymous labels such as Community A and Community B."
        }

    private fun hasSafetyRisk(text: String): Boolean =
        text.containsAny("threat", "armed", "machete", "gun", "attack", "violence", "weapon", "retaliation", "revenge", "killed")

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { contains(it) }
}
