
package com.stepblocks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.Template
import com.stepblocks.repository.TemplateRepository
import kotlinx.coroutines.launch

class AddEditTemplateViewModel(
    private val repository: TemplateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var templateName by mutableStateOf("")
        private set

    var templateId: Long? = savedStateHandle.get<Long>("templateId")?.takeIf { it != -1L }
        private set

    init {
        templateId?.let { id ->
            viewModelScope.launch {
                repository.getTemplateById(id)?.let {
                    templateName = it.name
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        templateName = newName
    }

    fun saveTemplate() {
        viewModelScope.launch {
            val template = Template(
                id = templateId ?: 0,
                name = templateName
            )
            repository.insertTemplate(template)
        }
    }
}
