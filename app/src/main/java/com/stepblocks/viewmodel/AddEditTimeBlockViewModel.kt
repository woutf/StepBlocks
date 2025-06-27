
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
    val targetSteps: String = "", // Add targetSteps to UI state
    val isTimeBlockSaved: Boolean = false,
    val isEditing: Boolean = false
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
                            targetSteps = timeBlock.targetSteps.toString(), // Load existing steps
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

    // Function to handle changes from the TextField
    fun onTargetStepsChange(steps: String) {
        if (steps.all { it.isDigit() }) { // Basic validation
            _uiState.update { it.copy(targetSteps = steps) }
        }
    }

    fun saveTimeBlock() {
        viewModelScope.launch {
            val currentState = _uiState.value
            // Use the value from the state, defaulting to 0 if empty
            val steps = currentState.targetSteps.toIntOrNull() ?: 0

            val timeBlock = TimeBlock(
                id = timeBlockId ?: 0,
                templateId = templateId,
                name = currentState.name,
                startTime = LocalTime.parse(currentState.startTime),
                endTime = LocalTime.parse(currentState.endTime),
                targetSteps = steps, // Save the actual steps
                notifyStart = false,
                notifyMid = false,
                notifyEnd = false
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
