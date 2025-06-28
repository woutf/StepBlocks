package com.stepblocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.Settings
import com.stepblocks.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = settings?.let {
                    SettingsUiState(
                        progressUpdatesEnabled = it.progressUpdatesEnabled,
                        beginBlockUpdates = it.beginBlockUpdates,
                        midBlockUpdates = it.midBlockUpdates,
                        endBlockUpdates = it.endBlockUpdates,
                        behindTargetPattern = it.behindTargetPattern,
                        onTargetPattern = it.onTargetPattern,
                        aheadTargetPattern = it.aheadTargetPattern
                    )
                } ?: SettingsUiState() // Use default if no settings exist
            }
        }
    }

    fun onProgressUpdatesToggleChange(isEnabled: Boolean) {
        _uiState.update { it.copy(progressUpdatesEnabled = isEnabled) }
        saveSettings()
    }

    fun onBeginBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.update { it.copy(beginBlockUpdates = isEnabled) }
        saveSettings()
    }

    fun onMidBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.update { it.copy(midBlockUpdates = isEnabled) }
        saveSettings()
    }

    fun onEndBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.update { it.copy(endBlockUpdates = isEnabled) }
        saveSettings()
    }

    fun onBehindTargetPatternChange(pattern: VibrationPattern) {
        _uiState.update { it.copy(behindTargetPattern = pattern) }
        saveSettings()
    }

    fun onOnTargetPatternChange(pattern: VibrationPattern) {
        _uiState.update { it.copy(onTargetPattern = pattern) }
        saveSettings()
    }

    fun onAheadTargetPatternChange(pattern: VibrationPattern) {
        _uiState.update { it.copy(aheadTargetPattern = pattern) }
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            _uiState.value.let { currentState ->
                settingsRepository.updateSettings(
                    Settings(
                        progressUpdatesEnabled = currentState.progressUpdatesEnabled,
                        beginBlockUpdates = currentState.beginBlockUpdates,
                        midBlockUpdates = currentState.midBlockUpdates,
                        endBlockUpdates = currentState.endBlockUpdates,
                        behindTargetPattern = currentState.behindTargetPattern,
                        onTargetPattern = currentState.onTargetPattern,
                        aheadTargetPattern = currentState.aheadTargetPattern
                    )
                )
            }
        }
    }
}
