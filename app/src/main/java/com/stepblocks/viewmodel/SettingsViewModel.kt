package com.stepblocks.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val masterNotificationToggleEnabled: Boolean = true
)

open class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    open val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    open fun onMasterNotificationToggleChange(isEnabled: Boolean) {
        _uiState.update { it.copy(masterNotificationToggleEnabled = isEnabled) }
    }
}
