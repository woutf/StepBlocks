package com.stepblocks.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.HealthConnectManager
import com.stepblocks.data.AppDatabase
import com.stepblocks.data.Template
import com.stepblocks.data.TimeBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class TodayScreenUiState(
    val totalDailySteps: Long = 0L,
    val currentBlockSteps: Long = 0L,
    val currentBlockName: String = "-",
    val currentBlockTarget: Int = 0,
    val timeRemainingInBlock: String = "",
    val dailyTarget: Int = 0,
    val permissionsGranted: Boolean = false,
    val isLoading: Boolean = true,
    val currentTemplate: Template? = null,
    val activeTimeBlock: TimeBlock? = null,
    val showPermissionRationale: Boolean = false
)

class TodayViewModel(
    application: Application,
    val healthConnectManager: HealthConnectManager,
    // Placeholder for DAOs, will be properly injected later
    // private val dayAssignmentDao: DayAssignmentDao,
    // private val templateDao: TemplateDao,
    // private val val dailyProgressDao: DailyProgressDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TodayScreenUiState())
    val uiState: StateFlow<TodayScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // Set loading true at the start
            val hasPermissions = healthConnectManager.hasAllPermissions() // Check permissions first
            _uiState.value = _uiState.value.copy(
                permissionsGranted = hasPermissions,
                showPermissionRationale = !hasPermissions,
                isLoading = false // Set loading false after initial check
            )
            if (hasPermissions) {
                fetchHealthConnectData() // Only fetch data if permissions are granted
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        _uiState.value = _uiState.value.copy(
            permissionsGranted = healthConnectManager.permissions.all { it in granted },
            showPermissionRationale = false
        )
        if (uiState.value.permissionsGranted) {
            fetchHealthConnectData()
        }
    }

    fun dismissPermissionRationale() {
        _uiState.value = _uiState.value.copy(showPermissionRationale = false)
    }

    private fun fetchHealthConnectData() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)

        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val now = ZonedDateTime.now(ZoneId.systemDefault()).toInstant()

        // Mock template and time blocks for now, using correct constructors
        val mockTemplateId = 1L // Assuming a template ID for these blocks
        val mockTimeBlocks = listOf(
            TimeBlock(id = 1, templateId = mockTemplateId, name = "Morning", startTime = LocalTime.of(6, 0), endTime = LocalTime.of(9, 0), targetSteps = 2000, notifyStart = false, notifyMid = false, notifyEnd = false),
            TimeBlock(id = 2, templateId = mockTemplateId, name = "Work AM", startTime = LocalTime.of(9, 0), endTime = LocalTime.of(12, 0), targetSteps = 3000, notifyStart = false, notifyMid = false, notifyEnd = false),
            TimeBlock(id = 3, templateId = mockTemplateId, name = "Lunch", startTime = LocalTime.of(12, 0), endTime = LocalTime.of(13, 0), targetSteps = 1000, notifyStart = false, notifyMid = false, notifyEnd = false),
            TimeBlock(id = 4, templateId = mockTemplateId, name = "Work PM", startTime = LocalTime.of(13, 0), endTime = LocalTime.of(17, 0), targetSteps = 4000, notifyStart = false, notifyMid = false, notifyEnd = false),
            TimeBlock(id = 5, templateId = mockTemplateId, name = "Evening", startTime = LocalTime.of(17, 0), endTime = LocalTime.of(23, 59), targetSteps = 2000, notifyStart = false, notifyMid = false, notifyEnd = false)
        )
        val mockTemplate = Template(id = mockTemplateId, name = "Mock Daily Template")

        _uiState.value = _uiState.value.copy(
            currentTemplate = mockTemplate,
            dailyTarget = mockTimeBlocks.sumOf { it.targetSteps } // Sum from mockTimeBlocks
        )

        val totalDailySteps = healthConnectManager.readSteps(
            startOfDay,
            now
        )

        val activeBlock = mockTimeBlocks.find { block -> // Use mockTimeBlocks here
            val blockStartInstant = ZonedDateTime.of(today, block.startTime, ZoneId.systemDefault()).toInstant()
            val blockEndInstant = ZonedDateTime.of(today, block.endTime, ZoneId.systemDefault()).toInstant()
            now.isAfter(blockStartInstant) && now.isBefore(blockEndInstant)
        }

        val currentBlockSteps = if (activeBlock != null) {
            val blockStartInstant = ZonedDateTime.of(today, activeBlock.startTime, ZoneId.systemDefault()).toInstant()
            healthConnectManager.readSteps(
                blockStartInstant,
                now
            )
        } else null

        val timeRemaining = if (activeBlock != null) {
            val blockEndDateTime = ZonedDateTime.of(today, activeBlock.endTime, ZoneId.systemDefault())
            val minutesRemaining = ChronoUnit.MINUTES.between(ZonedDateTime.now(ZoneId.systemDefault()), blockEndDateTime)
            if (minutesRemaining > 0) {
                val hours = minutesRemaining / 60
                val minutes = minutesRemaining % 60
                "%dh %02dm left".format(hours, minutes)
            } else "Block ended"
        } else "No active block"

        _uiState.value = _uiState.value.copy(
            totalDailySteps = totalDailySteps ?: 0L,
            activeTimeBlock = activeBlock,
            currentBlockName = activeBlock?.name ?: "No active block",
            currentBlockTarget = activeBlock?.targetSteps ?: 0,
            currentBlockSteps = currentBlockSteps ?: 0L,
            timeRemainingInBlock = timeRemaining,
            isLoading = false
        )
    }
}

class TodayViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            val healthConnectManager = HealthConnectManager(application.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return TodayViewModel(
                application = application,
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
