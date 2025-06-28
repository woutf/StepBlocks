package com.stepblocks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.DayAssignment
import com.stepblocks.data.DayAssignmentDao
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.TemplateDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val dayAssignmentDao: DayAssignmentDao,
    private val templateDao: TemplateDao
) : ViewModel() {

    val templates = templateDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val assignments = dayAssignmentDao.getAllAssignments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onAssignmentChange(day: DayOfWeek, templateId: Long) {
        viewModelScope.launch {
            dayAssignmentDao.upsert(DayAssignment(templateId, day))
        }
    }
}

class ScheduleViewModelFactory(
    private val dayAssignmentDao: DayAssignmentDao,
    private val templateDao: TemplateDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(dayAssignmentDao, templateDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}