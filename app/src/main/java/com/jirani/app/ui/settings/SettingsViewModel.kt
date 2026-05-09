package com.jirani.app.ui.settings

import androidx.lifecycle.ViewModel
import com.jirani.app.data.local.LocalFirstUiStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val code: String = "",
    val confirmCode: String = "",
    val message: String = "Use digits followed by equals, for example 2468=.",
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = sanitize(code), message = "Use digits followed by equals.") }
    }

    fun updateConfirmCode(code: String) {
        _uiState.update { it.copy(confirmCode = sanitize(code), message = "Use digits followed by equals.") }
    }

    fun saveCode() {
        val state = _uiState.value
        val valid = state.code.length in 4..9 &&
            state.code.endsWith("=") &&
            state.code.dropLast(1).all { it.isDigit() }

        when {
            !valid -> _uiState.update {
                it.copy(message = "Code must be 3-8 digits followed by equals.")
            }
            state.code != state.confirmCode -> _uiState.update {
                it.copy(message = "Codes do not match.")
            }
            else -> {
                LocalFirstUiStore.updateDiscreetCode(state.code)
                _uiState.update {
                    it.copy(
                        code = "",
                        confirmCode = "",
                        message = "Discreet calculator code updated.",
                    )
                }
            }
        }
    }

    private fun sanitize(value: String): String =
        value.filter { it.isDigit() || it == '=' }.take(9)
}
