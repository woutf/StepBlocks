
package com.stepblocks.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.stepblocks.StepBlocksApplication
import com.stepblocks.data.TimeBlock
import com.stepblocks.repository.TemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

interface ITimeBlocksViewModel {
    val timeBlocks: StateFlow<List<TimeBlock>>
}

class TimeBlocksViewModel(
    private val repository: TemplateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ITimeBlocksViewModel {

    private val templateId: Long = checkNotNull(savedStateHandle["templateId"])

    override val timeBlocks: StateFlow<List<TimeBlock>> =
        repository.getTimeBlocksForTemplate(templateId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}

class TimeBlocksViewModelFactory(
    private val templateId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as StepBlocksApplication
        val repository = application.repository // Corrected property name
        val savedStateHandle = extras.createSavedStateHandle()
        // Manually add templateId to savedStateHandle
        savedStateHandle["templateId"] = templateId

        if (modelClass.isAssignableFrom(TimeBlocksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeBlocksViewModel(repository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
