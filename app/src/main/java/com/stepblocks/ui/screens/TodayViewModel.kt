package com.stepblocks.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stepblocks.data.AppDatabase
import com.stepblocks.data.DayAssignment
import com.stepblocks.data.DayAssignmentDao
import com.stepblocks.data.HealthConnectManager
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateDao
import com.stepblocks.data.TimeBlock
import com.stepblocks.data.toDayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
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
    val showPermissionRationale: Boolean = false,
    val showNoTemplateMessage: Boolean = false,
)

class TodayViewModel(
    application: Application,
    val healthConnectManager: HealthConnectManager,
    private val dayAssignmentDao: DayAssignmentDao,
    private val templateDao: TemplateDao,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TodayScreenUiState())
    val uiState: StateFlow<TodayScreenUiState> = _uiState.asStateFlow()

    val templates = templateDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    suspend fun loadData() {

        val hasPermissions = healthConnectManager.hasAllPermissions()
        _uiState.value = _uiState.value.copy(
            permissionsGranted = hasPermissions,
            showPermissionRationale = !hasPermissions,
        )
        if (hasPermissions) {
            refreshData()
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        _uiState.value = _uiState.value.copy(
            permissionsGranted = healthConnectManager.permissions.all { it in granted },
            showPermissionRationale = false
        )
        if (uiState.value.permissionsGranted) {
            viewModelScope.launch { // Launch a coroutine for suspend call
                refreshData()
            }
        }
    }

    fun dismissPermissionRationale() {
        _uiState.value = _uiState.value.copy(showPermissionRationale = false)
    }

    fun assignTemplateToToday(templateId: Long) {
        viewModelScope.launch {
            val today = LocalDate.now().dayOfWeek.toDayOfWeek()
            dayAssignmentDao.upsert(DayAssignment(today, templateId)) // Changed order of arguments
            _uiState.value = _uiState.value.copy(showNoTemplateMessage = false)
            refreshData()
        }
    }

    private suspend fun refreshData() {


        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val now = ZonedDateTime.now(ZoneId.systemDefault()).toInstant()
        val dayOfWeek = today.dayOfWeek.toDayOfWeek()

        val dayAssignment = dayAssignmentDao.getDayAssignmentForDay(dayOfWeek)

        if (dayAssignment == null) {
            _uiState.value = _uiState.value.copy(
                showNoTemplateMessage = true,
                isLoading = false
            )
            return
        }

        val templateWithTimeBlocks = templateDao.getTemplateWithTimeBlocks(dayAssignment.templateId).firstOrNull()

        if (templateWithTimeBlocks == null) {
            _uiState.value = _uiState.value.copy(
                showNoTemplateMessage = true,
                isLoading = false
            )
            return
        }

        val template = templateWithTimeBlocks.template
        val timeBlocks = templateWithTimeBlocks.timeBlocks

        _uiState.value = _uiState.value.copy(
            currentTemplate = template,
            dailyTarget = timeBlocks.sumOf { it.targetSteps }
        )

        val totalDailySteps = healthConnectManager.readSteps(startOfDay, now)

        val activeBlock = timeBlocks.find { block ->
            val blockStartInstant = ZonedDateTime.of(today, block.startTime, ZoneId.systemDefault()).toInstant()
            val blockEndInstant = ZonedDateTime.of(today, block.endTime, ZoneId.systemDefault()).toInstant()
            now.isAfter(blockStartInstant) && now.isBefore(blockEndInstant)
        }

        val currentBlockSteps = if (activeBlock != null) {
            val blockStartInstant = ZonedDateTime.of(today, activeBlock.startTime, ZoneId.systemDefault()).toInstant()
            healthConnectManager.readSteps(blockStartInstant, now)
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
            val db = AppDatabase.getDatabase(application)
            @Suppress("UNCHECKED_CAST")
            return TodayViewModel(
                application = application,
                healthConnectManager = healthConnectManager,
                dayAssignmentDao = db.dayAssignmentDao(),
                templateDao = db.templateDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}