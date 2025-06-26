
package com.stepblocks.viewmodel

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.stepblocks.repository.TemplateRepository

class AddEditTemplateViewModelFactory(
    private val repository: TemplateRepository,
    private val savedStateHandle: SavedStateHandle
) : AbstractSavedStateViewModelFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(AddEditTemplateViewModel::class.java)) {
            return AddEditTemplateViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
