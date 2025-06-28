
package com.stepblocks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.TimeBlock
import com.stepblocks.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AddEditTimeBlockUiState(
    val name: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val targetSteps: String = "",
    val notifyStart: Boolean = false,
    val notifyMid: Boolean = false,
    val notifyEnd: Boolean = false,
    val isTimeBlockSaved: Boolean = false,
    val isEditing: Boolean = false,
    val targetStepsError: String? = null // Add error state for targetSteps
)

class AddEditTimeBlockViewModel(
    private val repository: TemplateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTimeBlockUiState())
    val uiState = _uiState.asStateFlow()

    private val templateId: Long = savedStateHandle.get<Long>("templateId")!!
    private var timeBlockId: Long? = savedStateHandle.get<Long>("timeBlockId")?.takeIf { it != -1L }

    init {
        timeBlockId?.let { id ->
            viewModelScope.launch {
                repository.getTimeBlockById(id)?.let { timeBlock ->
                    _uiState.update {
                        it.copy(
                            name = timeBlock.name,
                            startTime = timeBlock.startTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                            endTime = timeBlock.endTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                            targetSteps = timeBlock.targetSteps.toString(),
                            notifyStart = timeBlock.notifyStart,
                            notifyMid = timeBlock.notifyMid,
                            notifyEnd = timeBlock.notifyEnd,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onStartTimeChange(startTime: String) {
        _uiState.update { it.copy(startTime = startTime) }
    }

    fun onEndTimeChange(endTime: String) {
        _uiState.update { it.copy(endTime = endTime) }
    }

    fun onTargetStepsChange(steps: String) {
        if (steps.all { it.isDigit() }) {
            _uiState.update { it.copy(targetSteps = steps, targetStepsError = null) }
        } else if (steps.isEmpty()) {
            _uiState.update { it.copy(targetSteps = steps, targetStepsError = null) } // Clear error if empty
        }
    }

    fun onNotifyStartChange(isEnabled: Boolean) {
        _uiState.update { it.copy(notifyStart = isEnabled) }
    }

    fun onNotifyMidChange(isEnabled: Boolean) {
        _uiState.update { it.copy(notifyMid = isEnabled) }
    }

    fun onNotifyEndChange(isEnabled: Boolean) {
        _uiState.update { it.copy(notifyEnd = isEnabled) }
    }

    fun saveTimeBlock() {
        viewModelScope.launch {
            val currentState = _uiState.value

            val steps = currentState.targetSteps.toIntOrNull()
            if (steps == null || steps <= 0) {
                _uiState.update { it.copy(targetStepsError = "Steps must be a positive number") }
                return@launch
            } else {
                _uiState.update { it.copy(targetStepsError = null) }
            }

            val timeBlock = TimeBlock(
                id = timeBlockId ?: 0,
                templateId = templateId,
                name = currentState.name,
                startTime = LocalTime.parse(currentState.startTime),
                endTime = LocalTime.parse(currentState.endTime),
                targetSteps = steps,
                notifyStart = currentState.notifyStart,
                notifyMid = currentState.notifyMid,
                notifyEnd = currentState.notifyEnd
            )

            if (timeBlockId == null) {
                repository.insertTimeBlock(timeBlock)
            } else {
                repository.updateTimeBlock(timeBlock)
            }
            
            _uiState.update { it.copy(isTimeBlockSaved = true) }
        }
    }
}
