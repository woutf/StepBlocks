package com.stepblocks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.DayAssignment
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.data.TimeBlock
import com.stepblocks.repository.TemplateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Interface remains the same for the View
interface ITimeBlocksViewModel {
    val templateWithTimeBlocks: StateFlow<TemplateWithTimeBlocks?>
    val timeBlocks: StateFlow<List<TimeBlock>>
    val assignedDays: StateFlow<Set<DayOfWeek>>
    val editableTemplateName: StateFlow<String>
    val showNoTimeBlocksError: StateFlow<Boolean> // Added for validation
    fun deleteTimeBlock(timeBlock: TimeBlock)
    fun toggleDayAssignment(dayOfWeek: DayOfWeek)
    fun updateTemplateName(newName: String)
}

class TimeBlocksViewModel(
    private val repository: TemplateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ITimeBlocksViewModel {

    private val templateId: Long = checkNotNull(savedStateHandle["templateId"])

    private val templateWithTimeBlocksFlow: Flow<TemplateWithTimeBlocks?> =
        repository.getTemplateWithTimeBlocks(templateId)
            .distinctUntilChanged()
            .onEach { data ->
                // This onEach block is useful for debugging, but not strictly necessary for logic
                // println("TemplateWithTimeBlocks updated: ${data?.template?.name}, blocks: ${data?.timeBlocks?.size}")
            }

    override val templateWithTimeBlocks: StateFlow<TemplateWithTimeBlocks?> =
        templateWithTimeBlocksFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = null
            )

    override val timeBlocks: StateFlow<List<TimeBlock>> =
        templateWithTimeBlocks
            .map { it?.timeBlocks ?: emptyList() }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    override val assignedDays: StateFlow<Set<DayOfWeek>> =
        repository.getDayAssignmentsForTemplate(templateId)
            .map { assignments -> assignments.map { it.dayOfWeek }.toSet() }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptySet()
            )

    override val editableTemplateName: StateFlow<String> =
        templateWithTimeBlocks
            .map { it?.template?.name ?: "" }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = ""
            )

    private val _showNoTimeBlocksError = MutableStateFlow(false)
    override val showNoTimeBlocksError: StateFlow<Boolean> = _showNoTimeBlocksError.asStateFlow()

    init {
        // Observe both templateWithTimeBlocks and timeBlocks to determine error state
        combine(templateWithTimeBlocks, timeBlocks) { templateData, blocks ->
            // Show error if template is loaded but has no time blocks
            templateData != null && blocks.isEmpty()
        }
        .distinctUntilChanged() // Only emit when the error state actually changes
        .onEach { shouldShowError ->
            _showNoTimeBlocksError.value = shouldShowError
        }
        .launchIn(viewModelScope)
    }

    override fun updateTemplateName(newName: String) {
        viewModelScope.launch {
            val currentTemplate = templateWithTimeBlocks.value?.template
            if (currentTemplate != null && currentTemplate.name != newName) {
                repository.updateTemplate(currentTemplate.copy(name = newName))
            }
        }
    }

    override fun deleteTimeBlock(timeBlock: TimeBlock) {
        viewModelScope.launch {
            repository.deleteTimeBlock(timeBlock)
        }
    }

    override fun toggleDayAssignment(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            if (assignedDays.value.contains(dayOfWeek)) {
                repository.deleteDayAssignment(templateId, dayOfWeek)
            } else {
                repository.insertDayAssignment(DayAssignment(templateId = templateId, dayOfWeek = dayOfWeek))
            }
        }
    }
}
