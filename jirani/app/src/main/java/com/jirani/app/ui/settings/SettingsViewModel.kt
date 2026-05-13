package com.jirani.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jirani.app.data.local.AppLanguage
import com.jirani.app.data.local.AppThemeMode
import com.jirani.app.data.local.LocalFirstUiStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val code: String = "",
    val confirmCode: String = "",
    val nearbySharingEnabled: Boolean = true,
    val activeRelayModeEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.English,
    val themeMode: AppThemeMode = AppThemeMode.Light,
    val message: String = "",
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = combine(_uiState, LocalFirstUiStore.securitySettings) { draft, settings ->
        draft.copy(
            nearbySharingEnabled = settings.nearbySharingEnabled,
            activeRelayModeEnabled = settings.activeRelayModeEnabled,
            language = settings.language,
            themeMode = settings.themeMode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = sanitize(code), message = "") }
    }

    fun updateConfirmCode(code: String) {
        _uiState.update { it.copy(confirmCode = sanitize(code), message = "") }
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

    fun updateNearbySharing(enabled: Boolean) {
        LocalFirstUiStore.updateNearbySharingEnabled(enabled)
        _uiState.update {
            it.copy(
                message = if (enabled) {
                    "Nearby sharing enabled."
                } else {
                    "Nearby sharing paused. Reports stay queued on this phone."
                },
            )
        }
    }

    fun updateActiveRelayMode(enabled: Boolean) {
        LocalFirstUiStore.updateActiveRelayModeEnabled(enabled)
        _uiState.update {
            it.copy(
                message = if (enabled) {
                    "Active relay mode is on."
                } else {
                    "Active relay mode is off."
                },
            )
        }
    }

    fun updateLanguage(language: AppLanguage) {
        LocalFirstUiStore.updateLanguage(language)
        _uiState.update { it.copy(message = "Language set to ${language.label}.") }
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        LocalFirstUiStore.updateThemeMode(themeMode)
        _uiState.update { it.copy(message = "Theme set to ${themeMode.label}.") }
    }

    private fun sanitize(value: String): String =
        value.filter { it.isDigit() || it == '=' }.take(9)
}

val AppLanguage.label: String
    get() = when (this) {
        AppLanguage.English -> "English"
        AppLanguage.Swahili -> "Swahili"
        AppLanguage.Somali -> "Somali"
        AppLanguage.Kamba -> "Kamba"
    }

val AppThemeMode.label: String
    get() = when (this) {
        AppThemeMode.System -> "System"
        AppThemeMode.Light -> "Light"
        AppThemeMode.Dark -> "Dark"
    }
