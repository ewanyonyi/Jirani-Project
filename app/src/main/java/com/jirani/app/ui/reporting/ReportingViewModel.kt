package com.jirani.app.ui.reporting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.domain.agent.ReportingAgent
import com.jirani.app.domain.agent.SafetyReportRequest
import com.jirani.app.sync.NearbySyncRuntime
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
    val submissionMessage: String? = null,
    val submitting: Boolean = false,
) {
    val detailsValid: Boolean
        get() = threatType.isNotBlank() && details.trim().length >= 10
}

class ReportingViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val reportingAgent = ReportingAgent()
    private val _uiState = MutableStateFlow(savedStateHandle.toReportingUiState())
    val uiState: StateFlow<ReportingUiState> = _uiState

    fun selectThreat(type: String) {
        _uiState.updateAndSave { it.copy(threatType = type) }
    }

    fun updateLocation(location: String) {
        _uiState.updateAndSave { it.copy(generalLocation = location) }
    }

    fun updateDetails(details: String) {
        _uiState.updateAndSave { it.copy(details = details) }
    }

    fun nextStep() {
        _uiState.updateAndSave {
            when (it.step) {
                ReportStep.Threat -> if (it.detailsValid) it.copy(step = ReportStep.Location) else it
                ReportStep.Location -> it.copy(step = ReportStep.Verify)
                ReportStep.Verify -> it
            }
        }
    }

    fun submitLocalReport() {
        val state = _uiState.value
        if (!state.detailsValid) return
        _uiState.updateAndSave { it.copy(submitting = true) }
        val guidance = reportingAgent.process(
            SafetyReportRequest("${state.threatType}. ${state.generalLocation}. ${state.details}"),
        )
        val receipt = LocalFirstUiStore.submitSafetyReport(
            reportType = guidance.threatType,
            generalLocation = state.generalLocation,
            details = state.details,
        )
        if (LocalFirstUiStore.securitySettings.value.nearbySharingEnabled) {
            NearbySyncRuntime.ensureAvailable()
        }
        _uiState.updateAndSave {
            it.copy(
                step = ReportStep.Threat,
                threatType = "",
                generalLocation = "",
                details = "",
                submissionMessage = receipt.message,
                submitting = false,
            )
        }
    }

    private fun MutableStateFlow<ReportingUiState>.updateAndSave(
        transform: (ReportingUiState) -> ReportingUiState,
    ) {
        update { current ->
            transform(current).also { savedStateHandle.save(it) }
        }
    }

    private fun SavedStateHandle.toReportingUiState(): ReportingUiState =
        ReportingUiState(
            step = get<String>(StepKey)?.let { ReportStep.valueOf(it) } ?: ReportStep.Threat,
            threatType = get<String>(ThreatTypeKey).orEmpty(),
            generalLocation = get<String>(GeneralLocationKey).orEmpty(),
            details = get<String>(DetailsKey).orEmpty(),
            submissionMessage = get<String>(SubmissionMessageKey),
            submitting = get<Boolean>(SubmittingKey) ?: false,
        )

    private fun SavedStateHandle.save(state: ReportingUiState) {
        this[StepKey] = state.step.name
        this[ThreatTypeKey] = state.threatType
        this[GeneralLocationKey] = state.generalLocation
        this[DetailsKey] = state.details
        this[SubmissionMessageKey] = state.submissionMessage
        this[SubmittingKey] = state.submitting
    }

    private companion object {
        const val StepKey = "report_step"
        const val ThreatTypeKey = "report_threat_type"
        const val GeneralLocationKey = "report_general_location"
        const val DetailsKey = "report_details"
        const val SubmissionMessageKey = "report_submission_message"
        const val SubmittingKey = "report_submitting"
    }
}
