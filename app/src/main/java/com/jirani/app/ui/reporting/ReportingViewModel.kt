package com.jirani.app.ui.reporting

import androidx.lifecycle.ViewModel
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.data.local.SyncEnvelope
import com.jirani.app.domain.agent.ReportingAgent
import com.jirani.app.domain.agent.SafetyReportGuidance
import com.jirani.app.domain.agent.SafetyReportRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class ReportStep {
    Threat,
    Location,
    Verify,
}

data class ReportingUiState(
    val step: ReportStep = ReportStep.Threat,
    val threatType: String = "",
    val generalLocation: String = "",
    val details: String = "",
    val guidance: SafetyReportGuidance? = null,
    val syncEnvelope: SyncEnvelope? = null,
)

class ReportingViewModel : ViewModel() {
    private val reportingAgent = ReportingAgent()
    private val _uiState = MutableStateFlow(ReportingUiState())
    val uiState: StateFlow<ReportingUiState> = _uiState

    fun selectThreat(type: String) {
        _uiState.update { it.copy(threatType = type, step = ReportStep.Location) }
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(generalLocation = location) }
    }

    fun updateDetails(details: String) {
        _uiState.update { it.copy(details = details) }
    }

    fun continueToVerify() {
        _uiState.update { it.copy(step = ReportStep.Verify) }
    }

    fun submitLocalReport() {
        val state = _uiState.value
        val guidance = reportingAgent.process(
            SafetyReportRequest("${state.threatType}. ${state.generalLocation}. ${state.details}"),
        )
        val envelope = LocalFirstUiStore.saveSafetyReport(
            reportType = guidance.threatType,
            generalLocation = state.generalLocation,
            details = state.details,
        )
        _uiState.update { it.copy(guidance = guidance, syncEnvelope = envelope) }
    }
}
