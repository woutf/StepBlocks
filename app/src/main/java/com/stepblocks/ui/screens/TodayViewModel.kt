package com.stepblocks.ui.screens

//import android.app.Application
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import com.stepblocks.data.BlockProgress
//import com.stepblocks.data.BlockProgressDao
//import com.stepblocks.data.DailyProgress
//import com.stepblocks.data.DailyProgressDao
//import com.stepblocks.data.DayAssignmentDao
//import com.stepblocks.data.HealthConnectManager
//import com.stepblocks.data.Template
//import com.stepblocks.data.TimeBlock
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import java.time.DayOfWeek
//import java.time.Instant
//import java.time.LocalDate
//import java.time.LocalTime
//import java.time.ZoneId
//
//class TodayViewModel(
//    private val application: Application,
//    private val dayAssignmentDao: DayAssignmentDao,
//    private val dailyProgressDao: DailyProgressDao,
//    private val blockProgressDao: BlockProgressDao,
//    private val healthConnectManager: HealthConnectManager
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(TodayUiState())
//    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            loadTodayData()
//        }
//    }
//
//    private suspend fun loadTodayData() {
//        val today = LocalDate.now()
//        val dayOfWeek = today.dayOfWeek.value // 1 = Monday, 7 = Sunday
//
//        // 1. Determine the current day's template
//        val assignedTemplateId = dayAssignmentDao.getDayAssignmentForDay(dayOfWeek)?.templateId
//        val currentTemplate = assignedTemplateId?.let { dailyProgressDao.getTemplateById(it) }
//
//        if (currentTemplate == null) {
//            _uiState.value = _uiState.value.copy(
//                isLoading = false,
//                templateName = "No Template Assigned for Today",
//                dailyTargetSteps = 0
//            )
//            return
//        }
//
//        // 4. Fetch or create today's DailyProgress and BlockProgress
//        var dailyProgress = dailyProgressDao.getDailyProgressByDate(today)
//        if (dailyProgress == null) {
//            dailyProgress = DailyProgress(
//                date = today,
//                templateId = currentTemplate.template.templateId,
//                blockProgress = currentTemplate.timeBlocks.map { timeBlock ->
//                    BlockProgress(
//                        blockName = timeBlock.blockName,
//                        targetSteps = timeBlock.targetSteps,
//                        actualSteps = 0,
//                        startNotificationSent = false,
//                        midNotificationSent = false,
//                        endNotificationSent = false
//                    )
//                }
//            )
//            dailyProgressDao.insertDailyProgress(dailyProgress)
//            dailyProgress = dailyProgressDao.getDailyProgressByDate(today) // Re-fetch to get ID
//        }
//
//        val updatedBlockProgress = mutableListOf<BlockProgress>()
//        for (block in dailyProgress?.blockProgress ?: emptyList()) {
//            val correspondingTimeBlock = currentTemplate.timeBlocks.find { it.blockName == block.blockName }
//            val blockStartTime = today.atTime(correspondingTimeBlock?.startTime).atZone(ZoneId.systemDefault()).toInstant()
//            val blockEndTime = today.atTime(correspondingTimeBlock?.endTime).atZone(ZoneId.systemDefault()).toInstant()
//
//            // 3. Fetch steps for the current block
//            val blockSteps = healthConnectManager.getSteps(blockStartTime, blockEndTime)
//            updatedBlockProgress.add(block.copy(actualSteps = blockSteps.toInt()))
//        }
//        dailyProgress = dailyProgress?.copy(blockProgress = updatedBlockProgress)
//        dailyProgress?.let { dailyProgressDao.updateDailyProgress(it) } // Update in DB
//
//
//        // 2. Identify the currently active time block
//        val now = LocalTime.now()
//        val activeBlock = currentTemplate.timeBlocks.find { timeBlock ->
//            val start = timeBlock.startTime
//            val end = timeBlock.endTime
//            if (start <= end) {
//                now.isAfter(start.minusMinutes(1)) && now.isBefore(end.plusMinutes(1))
//            } else { // Block spans midnight, e.g., 22:00 - 02:00
//                now.isAfter(start.minusMinutes(1)) || now.isBefore(end.plusMinutes(1))
//            }
//        }
//
//        // 3. Fetch total steps for the entire day
//        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
//        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
//        val totalDailySteps = healthConnectManager.getSteps(startOfDay, endOfDay)
//
//        _uiState.value = _uiState.value.copy(
//            isLoading = false,
//            templateName = currentTemplate.template.name,
//            dailyTargetSteps = currentTemplate.timeBlocks.sumOf { it.targetSteps },
//            dailyActualSteps = totalDailySteps.toInt(),
//            blockProgress = dailyProgress?.blockProgress ?: emptyList(),
//            activeBlockName = activeBlock?.blockName,
//            activeBlockTargetSteps = activeBlock?.targetSteps ?: 0,
//            activeBlockActualSteps = dailyProgress?.blockProgress?.find { it.blockName == activeBlock?.blockName }?.actualSteps ?: 0
//        )
//    }
//
//    // You may want to expose a refresh function
//    fun refreshData() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
//            loadTodayData()
//        }
//    }
//
//    companion object {
//        fun provideFactory(
//            application: Application,
//            dayAssignmentDao: DayAssignmentDao,
//            dailyProgressDao: DailyProgressDao,
//            blockProgressDao: BlockProgressDao,
//            healthConnectManager: HealthConnectManager
//        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
//                    return TodayViewModel(
//                        application,
//                        dayAssignmentDao,
//                        dailyProgressDao,
//                        blockProgressDao,
//                        healthConnectManager
//                    ) as T
//                }
//                throw IllegalArgumentException("Unknown ViewModel class")
//            }
//        }
//    }
//}
//
//data class TodayUiState(
//    val isLoading: Boolean = true,
//    val templateName: String = "",
//    val dailyTargetSteps: Int = 0,
//    val dailyActualSteps: Int = 0,
//    val blockProgress: List<BlockProgress> = emptyList(),
//    val activeBlockName: String? = null,
//    val activeBlockTargetSteps: Int = 0,
//    val activeBlockActualSteps: Int = 0,
//    val paceStatus: String = "" // TODO: Implement pace status logic
//)
