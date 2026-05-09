package com.jirani.app.domain.agent

data class AgreementSummaryRequest(
    val issue: String,
    val commitments: List<String>,
)

data class AgreementSummary(
    val summary: String,
    val actions: List<String>,
    val followUp: String,
)

class SummaryAgent : JiraniAgent<AgreementSummaryRequest, AgreementSummary> {
    override val name: String = "Summary Agent"

    override fun process(input: AgreementSummaryRequest): AgreementSummary {
        val issue = input.issue.trim().ifBlank { "A community agreement was discussed." }
        val actions = input.commitments
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf("Confirm the agreement details with all parties before sharing.") }

        return AgreementSummary(
            summary = issue,
            actions = actions,
            followUp = "Review the agreement after the agreed period and update the local record if conditions change.",
        )
    }
}
