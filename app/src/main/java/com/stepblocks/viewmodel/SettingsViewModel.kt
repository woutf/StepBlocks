package com.stepblocks.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.Settings
import com.stepblocks.data.SettingsRepository
import com.stepblocks.data.TemplateDao
import com.stepblocks.data.TimeBlockDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val aheadTargetPattern: VibrationPattern = VibrationPattern.TRIPLE_TAP,
    val backupFileUri: Uri? = null
)

class SettingsViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val templateDao: TemplateDao,
    private val timeBlockDao: TimeBlockDao
) : AndroidViewModel(application) {

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

    fun onBackupTemplatesClick() {
        viewModelScope.launch {
            val templatesWithTimeBlocks = templateDao.getAllTemplatesWithTimeBlocks().first()
            val json = Json.encodeToString(templatesWithTimeBlocks)
            
            val context = getApplication<Application>().applicationContext
            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.format(Date())
            val backupFile = File(backupDir, "Backup-$date.json")
            backupFile.writeText(json)
            
            val backupFileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                backupFile
            )
            _uiState.update { it.copy(backupFileUri = backupFileUri) }
        }
    }

    fun onBackupShareComplete() {
        _uiState.update { it.copy(backupFileUri = null) }
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
