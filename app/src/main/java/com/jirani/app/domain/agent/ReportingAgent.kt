package com.jirani.app.domain.agent

data class SafetyReportRequest(
    val description: String,
)

data class SafetyReportGuidance(
    val incidentSummary: String,
    val threatType: String,
    val triageOutcome: String,
    val mediationReadiness: String,
    val localActorsToNotify: List<String>,
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
            triageOutcome = triageOutcome(lower),
            mediationReadiness = mediationReadiness(lower),
            localActorsToNotify = localActorsToNotify(lower),
            safeNextSteps = safeNextSteps(lower),
            missingNonIdentifyingDetails = missingDetails(lower),
        )
    }

    private fun classifyThreat(text: String): String = when {
        text.containsAny("gbv", "gender based", "sexual", "rape", "defilement") -> "GBV survivor safety report"
        text.containsAny("domestic", "husband", "wife", "partner abuse", "beating at home", "family violence") -> "domestic violence safety report"
        text.containsAny("camel", "goat", "cow", "rustler", "cattle", "livestock") -> "livestock or grazing dispute"
        text.containsAny("armed", "machete", "gun", "robber", "goon", "attack", "killed", "burned", "violence") -> "active violence or retaliation risk"
        text.containsAny("water", "well", "river", "grazing", "pasture", "boundary", "land") -> "resource access conflict"
        text.containsAny("rumor", "heard", "claim", "incitement") -> "rumor or incitement risk"
        text.containsAny("extremist", "terror", "militia") -> "extremist or organized threat"
        text.isBlank() -> "unclassified"
        else -> "community conflict report"
    }

    private fun triageOutcome(text: String): String = when {
        text.isBlank() -> "Add enough detail for a trusted local reviewer to understand the risk."
        isSurvivorCentered(text) -> "Survivor safety first: keep details private and share only with a trusted support actor chosen by the survivor."
        hasActiveViolence(text) -> "Protection first: do not start mediation while violence or retaliation is active."
        text.containsAny("rumor", "heard", "claim", "incitement") -> "Verify first: treat this as unconfirmed until two trusted local sources check it."
        text.containsAny("water", "well", "river", "grazing", "pasture", "boundary", "land", "camel", "cattle", "livestock") ->
            "Elder review: this can become mediation only after both sides are calm and reachable."
        else -> "Local review: a trusted person should check the facts before wider sharing."
    }

    private fun mediationReadiness(text: String): String = when {
        text.isBlank() -> "Not ready"
        isSurvivorCentered(text) -> "Not for mediation: do not convene the survivor with the alleged abuser or expose the report."
        hasActiveViolence(text) -> "Not ready: record the report, protect people, and wait for safety clearance."
        text.containsAny("rumor", "heard", "claim", "incitement") -> "Not ready: verify the claim before convening anyone."
        else -> "Possible after review by elders, a peace committee, chief, religious leader, or OSF/community partner."
    }

    private fun localActorsToNotify(text: String): List<String> = when {
        isSurvivorCentered(text) -> listOf(
            "survivor-chosen trusted support person",
            "trained GBV or child protection focal person if available",
            "health, legal, or psychosocial support provider chosen by the survivor",
        )
        hasActiveViolence(text) -> listOf(
            "nearest trusted elder or village leader",
            "local administration or chief",
            "community peace committee",
            "safe OSF/community partner contact if available",
        )
        text.containsAny("water", "well", "river", "grazing", "pasture", "boundary", "land", "camel", "cattle", "livestock") -> listOf(
            "elders from each affected community",
            "grazing or water committee representative",
            "neutral peace committee member",
            "OSF/community partner if the conflict crosses locations",
        )
        else -> listOf(
            "trusted local verifier",
            "elder or religious leader",
            "peace committee member",
        )
    }

    private fun safeNextSteps(text: String): List<String> {
        val base = mutableListOf(
            "Do not collect names, phone numbers, photos of faces, or exact homes.",
            "Write the general area, time window, what was observed, and who could verify it safely.",
        )

        if (isSurvivorCentered(text)) {
            base += "Do not notify family, elders, police, or the alleged abuser without survivor consent unless a child or immediate life-threatening danger requires urgent protection."
            base += "Share only through a private survivor-support path; do not create a community alert."
        } else if (hasActiveViolence(text)) {
            base += "Warn people away from the danger area through trusted channels; do not call both sides together."
            base += "Mark the case for protection and fact-checking before any mediation meeting."
        } else {
            base += "Ask a trusted elder or peace committee member to check whether both sides can be contacted safely."
            base += "If both sides agree, open a mediation case with anonymous labels such as Community A and Community B."
        }

        return base
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

    private fun hasActiveViolence(text: String): Boolean =
        text.containsAny("armed", "machete", "gun", "attack", "attacked", "killed", "burned", "retaliation", "revenge", "violence")

    private fun isSurvivorCentered(text: String): Boolean =
        text.containsAny("gbv", "gender based", "sexual", "rape", "defilement", "domestic", "husband", "wife", "partner abuse", "beating at home", "family violence")

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { contains(it) }
}
