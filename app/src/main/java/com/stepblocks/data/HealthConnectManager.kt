package com.stepblocks.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import java.io.IOException
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)

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

    suspend fun readSteps(startTime: Instant, endTime: Instant): Long? {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(
                        startTime,
                        endTime
                    )
                )
            )
            var totalSteps = 0L
            for (stepRecord in response.records) {
                totalSteps += stepRecord.count
            }
            return totalSteps
        } catch (e: Exception) {
            // Handle exceptions like permission not granted, API not available, etc.
            if (e is IOException) {
                // Handle IO exceptions (e.g., Health Connect app not installed or unavailable)
            }
            e.printStackTrace()
            return null
        }
    }
}
