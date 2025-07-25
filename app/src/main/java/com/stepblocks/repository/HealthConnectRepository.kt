package com.stepblocks.repository

import android.content.Context
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "HealthConnectRepo"

class HealthConnectRepository private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: HealthConnectRepository? = null
        fun getInstance(context: Context): HealthConnectRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HealthConnectRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun getPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient
            .permissionController
            .getGrantedPermissions()
            .containsAll(permissions)
    }

    private val _realtimeSteps = MutableStateFlow(0L)
    val realtimeSteps: StateFlow<Long> = _realtimeSteps.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    suspend fun getLastKnownTime(): Instant {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now()
            )
        )
        val response = healthConnectClient.readRecords(request)
        val lastTime = response.records.maxOfOrNull { it.endTime } ?: Instant.now().minus(1, ChronoUnit.DAYS)
        Log.d(TAG, "Last known time: $lastTime")
        return lastTime
    }

    private suspend fun <T> withRetry(
        times: Int = 5,
        initialDelay: Long = 2000,
        maxDelay: Long = 60000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                Log.w(TAG, "Retryable error: ${e.message}")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }

    suspend fun syncStepsToHealthConnect(stepDelta: Int, startTime: Instant, endTime: Instant) {
        val recordId = "stepblocks-steps-${startTime.toEpochMilli()}"

        val stepsRecord = StepsRecord(
            count = stepDelta.toLong(),
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime),
            endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endTime),
            metadata = Metadata.activelyRecorded(
                clientRecordId = recordId,
                clientRecordVersion = 1,
                device = Device(
                    manufacturer = "StepBlocks",
                    model = "Pixel Watch",
                    type = Device.TYPE_WATCH
                )
            )
        )

        try {
            withRetry {
                healthConnectClient.insertRecords(listOf(stepsRecord))
            }
            _realtimeSteps.value += stepDelta.toLong()
            Log.d(TAG, "Synced $stepDelta steps to Health Connect")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync steps to Health Connect", e)
        }
    }

    fun updateRealtimeSteps(steps: Long) {
        _realtimeSteps.value = steps
        Log.d(TAG, "Realtime steps updated to: $steps")
    }

    fun updateConnectionStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
        Log.d(TAG, "Connection status updated to: $status")
    }
}

sealed class ConnectionStatus {
    object Connected : ConnectionStatus()
    object Disconnected : ConnectionStatus()
    object Syncing : ConnectionStatus()
}
