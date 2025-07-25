
package com.stepblocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.repository.TemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateViewModel(private val repository: TemplateRepository) : ViewModel() {

    val templates: StateFlow<List<TemplateWithTimeBlocks>> = repository.getAllTemplatesWithTimeBlocks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTemplate(template: Template) {
        viewModelScope.launch {
            repository.insertTemplate(template)
        }
    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }
}
