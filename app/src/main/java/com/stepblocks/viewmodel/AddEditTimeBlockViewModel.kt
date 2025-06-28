
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn // Explicit import for stateIn

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
    val targetStepsError: String? = null,
    val timeRangeError: String? = null,
    val overlapError: String? = null // Add error state for overlap
)

class AddEditTimeBlockViewModel(
    private val repository: TemplateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTimeBlockUiState())
    val uiState = _uiState.asStateFlow()

    private val templateId: Long = savedStateHandle.get<Long>("templateId")!!
    private var timeBlockId: Long? = savedStateHandle.get<Long>("timeBlockId")?.takeIf { it != -1L }

    // Collect all existing time blocks for the current template
    private val existingTimeBlocks: StateFlow<List<TimeBlock>> =
        repository.getTimeBlocksForTemplate(templateId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

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

        // Combine _uiState and existingTimeBlocks to re-validate on any relevant change
        combine(_uiState, existingTimeBlocks) { currentUiState, blocks ->
            val timeRangeError = validateTimeRange(currentUiState.startTime, currentUiState.endTime)
            val overlapError = if (timeRangeError == null) {
                checkOverlap(currentUiState.startTime, currentUiState.endTime, blocks)
            } else {
                null
            }
            Pair(timeRangeError, overlapError) // Return a Pair of errors
        }.onEach { (timeRangeError, overlapError) ->
            _uiState.update { currentState ->
                currentState.copy(
                    timeRangeError = timeRangeError,
                    overlapError = overlapError
                )
            }
        }.launchIn(viewModelScope)
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

    private fun validateTimeRange(startTimeStr: String, endTimeStr: String): String? {
        if (startTimeStr.isBlank() || endTimeStr.isBlank()) return null

        return try {
            val startTime = LocalTime.parse(startTimeStr)
            val endTime = LocalTime.parse(endTimeStr)

            if (endTime.isBefore(startTime) || endTime == startTime) {
                "End time must be after start time"
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle parsing errors, though the TimePicker should ideally prevent invalid formats
            null
        }
    }

    private fun checkOverlap(startTimeStr: String, endTimeStr: String, existingBlocks: List<TimeBlock>): String? {
        if (startTimeStr.isBlank() || endTimeStr.isBlank()) return null

        return try {
            val newStartTime = LocalTime.parse(startTimeStr)
            val newEndTime = LocalTime.parse(endTimeStr)

            val filteredBlocks = existingBlocks.filterNot { it.id == timeBlockId } // Exclude current block if editing

            for (block in filteredBlocks) {
                // Check for overlap:
                // (StartA < EndB) && (EndA > StartB)
                // New block starts before existing block ends AND new block ends after existing block starts
                if (newStartTime.isBefore(block.endTime) && newEndTime.isAfter(block.startTime)) {
                    return "Overlaps with existing block '${block.name}' (${block.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${block.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))})"
                }
            }
            null
        } catch (e: Exception) {
            null
        }
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

            // Re-validate all fields just before saving to ensure latest state
            val steps = currentState.targetSteps.toIntOrNull()
            if (steps == null || steps <= 0) {
                _uiState.update { it.copy(targetStepsError = "Steps must be a positive number") }
                return@launch
            }

            val timeRangeError = validateTimeRange(currentState.startTime, currentState.endTime)
            if (timeRangeError != null) {
                _uiState.update { it.copy(timeRangeError = timeRangeError) }
                return@launch
            }

            val overlapError = checkOverlap(currentState.startTime, currentState.endTime, existingTimeBlocks.value)
            if (overlapError != null) {
                _uiState.update { it.copy(overlapError = overlapError) }
                return@launch
            }

            // Clear any lingering errors if all validations pass
            _uiState.update { it.copy(targetStepsError = null, timeRangeError = null, overlapError = null) }

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
