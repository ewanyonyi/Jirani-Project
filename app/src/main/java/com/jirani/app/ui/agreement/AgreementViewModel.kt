package com.jirani.app.ui.agreement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jirani.app.data.local.AgreementRecordStatus
import com.jirani.app.data.local.AgreementItem
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.domain.agent.AgreementSummary
import com.jirani.app.domain.agent.AgreementSummaryRequest
import com.jirani.app.domain.agent.SummaryAgent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class VaultUiState(
    val search: String = "",
    val selectedFilter: AgreementRecordStatus? = null,
    val agreements: List<AgreementItem> = emptyList(),
    val issue: String = "",
    val commitments: String = "",
    val summary: AgreementSummary? = null,
)

class AgreementViewModel : ViewModel() {
    private val summaryAgent = SummaryAgent()
    private val draftState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = combine(
        draftState,
        LocalFirstUiStore.agreements,
    ) { draft, agreements ->
        val filtered = agreements.filter {
            val matchesSearch = draft.search.isBlank() ||
                it.title.contains(draft.search, ignoreCase = true) ||
                it.summary.contains(draft.search, ignoreCase = true)
            val matchesFilter = draft.selectedFilter == null || it.recordStatus == draft.selectedFilter
            matchesSearch && matchesFilter
        }
        draft.copy(agreements = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VaultUiState())

    fun updateSearch(search: String) {
        draftState.update { it.copy(search = search) }
    }

    fun toggleFilter(filter: AgreementRecordStatus) {
        draftState.update {
            it.copy(selectedFilter = if (it.selectedFilter == filter) null else filter)
        }
    }

    fun updateIssue(issue: String) {
        draftState.update { it.copy(issue = issue) }
    }

    fun updateCommitments(commitments: String) {
        draftState.update { it.copy(commitments = commitments) }
    }

    fun generateSummary() {
        val state = draftState.value
        val summary = summaryAgent.process(
            AgreementSummaryRequest(state.issue, state.commitments.lines()),
        )
        LocalFirstUiStore.saveAgreementDraft(
            title = state.issue,
            summary = summary.summary,
        )
        draftState.update { it.copy(summary = summary) }
    }
}
