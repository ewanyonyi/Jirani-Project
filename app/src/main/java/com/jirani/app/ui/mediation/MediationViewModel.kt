package com.jirani.app.ui.mediation

import androidx.lifecycle.ViewModel
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.domain.agent.MediationAgent
import com.jirani.app.domain.agent.MediationGuidance
import com.jirani.app.domain.agent.MediationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ChatMessage(
    val sender: String,
    val body: String,
)

data class MediationUiState(
    val input: String = "",
    val conflictType: String = "",
    val incidentType: String = "",
    val helpNeed: String = "",
    val neutralizedText: String = "Safe version appears here as you prepare a message.",
    val showToneCheck: Boolean = false,
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Jirani", "Describe the dispute in neutral terms. I will help de-escalate it."),
    ),
    val guidance: MediationGuidance? = null,
)

class MediationViewModel : ViewModel() {
    private val mediationAgent = MediationAgent()
    private val _uiState = MutableStateFlow(MediationUiState())
    val uiState: StateFlow<MediationUiState> = _uiState

    fun updateInput(input: String) {
        _uiState.update {
            it.copy(
                input = input,
                neutralizedText = neutralize(input),
                showToneCheck = looksHeated(input),
            )
        }
    }

    fun useChip(text: String) {
        updateInput(text)
    }

    fun selectConflictType(type: String) {
        _uiState.update { it.copy(conflictType = type) }
    }

    fun selectIncidentType(type: String) {
        _uiState.update { it.copy(incidentType = type) }
    }

    fun selectHelpNeed(need: String) {
        _uiState.update { it.copy(helpNeed = need) }
    }

    fun submit() {
        val state = _uiState.value
        val description = buildGuidedDescription(state)
        val guidance = mediationAgent.process(MediationRequest(description))
        _uiState.update {
            it.copy(
                input = "",
                guidance = guidance,
                showToneCheck = false,
                messages = it.messages + ChatMessage("You", description.ifBlank { "No details provided yet." }) +
                    ChatMessage("Jirani", guidance.recommendations.joinToString("\n")),
            )
        }
        LocalFirstUiStore.enqueueSync()
    }

    private fun neutralize(input: String): String =
        if (input.isBlank()) {
            "Safe version appears here as you prepare a message."
        } else {
            "We need a calm discussion to clarify facts, reduce tension, and agree on fair next steps."
        }

    fun useNeutralizedText() {
        _uiState.update {
            it.copy(
                input = it.neutralizedText,
                showToneCheck = false,
            )
        }
    }

    private fun looksHeated(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("angry") ||
            lower.contains("fight") ||
            lower.contains("blocked") ||
            lower.contains("stole") ||
            lower.contains("threat") ||
            lower.contains("attack") ||
            input.count { it == '!' } >= 2
    }

    private fun buildGuidedDescription(state: MediationUiState): String =
        listOf(
            state.conflictType.takeIf { it.isNotBlank() }?.let { "Conflict type: $it" },
            state.incidentType.takeIf { it.isNotBlank() }?.let { "What happened: $it" },
            state.helpNeed.takeIf { it.isNotBlank() }?.let { "Help needed: $it" },
            state.input.takeIf { it.isNotBlank() }?.let { "More detail: $it" },
        ).filterNotNull().joinToString(". ")
}
