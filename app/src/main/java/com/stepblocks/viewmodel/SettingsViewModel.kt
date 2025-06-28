package com.stepblocks.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class VibrationPattern {
    NONE,
    SHORT_DOUBLE_TAP,
    LONG_SINGLE,
    TRIPLE_TAP
}

data class SettingsUiState(
    val progressUpdatesEnabled: Boolean = true,
    val beginBlockUpdates: Boolean = true,
    val midBlockUpdates: Boolean = true,
    val endBlockUpdates: Boolean = true,
    val behindTargetPattern: VibrationPattern = VibrationPattern.SHORT_DOUBLE_TAP,
    val onTargetPattern: VibrationPattern = VibrationPattern.LONG_SINGLE,
    val aheadTargetPattern: VibrationPattern = VibrationPattern.TRIPLE_TAP
)

open class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    open val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    open fun onProgressUpdatesToggleChange(isEnabled: Boolean) {
        _uiState.update { it.copy(progressUpdatesEnabled = isEnabled) }
    }

    open fun onBeginBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.update { it.copy(beginBlockUpdates = isEnabled) }
    }

    open fun onMidBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.update { it.copy(midBlockUpdates = isEnabled) }
    }

    open fun onEndBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.update { it.copy(endBlockUpdates = isEnabled) }
    }

    open fun onBehindTargetPatternChange(pattern: VibrationPattern) {
        _uiState.update { it.copy(behindTargetPattern = pattern) }
    }

    open fun onOnTargetPatternChange(pattern: VibrationPattern) {
        _uiState.update { it.copy(onTargetPattern = pattern) }
    }

    open fun onAheadTargetPatternChange(pattern: VibrationPattern) {
        _uiState.update { it.copy(aheadTargetPattern = pattern) }
    }
}
