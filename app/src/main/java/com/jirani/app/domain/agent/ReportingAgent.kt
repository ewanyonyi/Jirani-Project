package com.jirani.app.domain.agent

data class SafetyReportRequest(
    val description: String,
)

data class SafetyReportGuidance(
    val incidentSummary: String,
    val threatType: String,
    val safeNextSteps: List<String>,
    val missingNonIdentifyingDetails: List<String>,
)

class ReportingAgent : JiraniAgent<SafetyReportRequest, SafetyReportGuidance> {
    override val name: String = "Reporting Agent"

    override fun process(input: SafetyReportRequest): SafetyReportGuidance {
        val description = input.description.trim()
        val lower = description.lowercase()
        return SafetyReportGuidance(
            incidentSummary = if (description.isBlank()) {
                "No safety incident details have been provided yet."
            } else {
                "A safety concern has been captured for trusted local verification."
            },
            threatType = classifyThreat(lower),
            safeNextSteps = listOf(
                "Avoid collecting names, phone numbers, or personal identifiers.",
                "Verify the report through trusted local safety monitors before broad sharing.",
                "Share only the general location, time window, and observed risk.",
            ),
            missingNonIdentifyingDetails = missingDetails(lower),
        )
    }

    private fun classifyThreat(text: String): String = when {
        text.containsAny("rustler", "cattle", "livestock") -> "cattle rustling risk"
        text.containsAny("armed", "machete", "robber", "goon") -> "armed or robbery risk"
        text.containsAny("extremist", "terror", "militia") -> "extremist or organized threat"
        text.isBlank() -> "unclassified"
        else -> "community safety alert"
    }

    private fun missingDetails(text: String): List<String> {
        if (text.isBlank()) return listOf("general location", "time window", "observed risk")

        val missing = mutableListOf<String>()
        if (!text.containsAny("near", "at", "road", "path", "market", "village", "river")) {
            missing += "general location"
        }
        if (!text.containsAny("today", "morning", "afternoon", "evening", "night", "yesterday")) {
            missing += "time window"
        }
        return missing
    }

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { contains(it) }
}
