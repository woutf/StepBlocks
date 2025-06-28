package com.stepblocks.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
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
        // TODO: Implement actual Health Connect API call to read steps
        // This will require handling permissions and potential errors.
        // For now, return a mock value or null.
        return null
    }

    suspend fun checkPermissionsAndReadSteps(
        startTime: Instant,
        endTime: Instant,
        permissionsGranted: (Boolean) -> Unit
    ): Long? {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissions)) {
                permissionsGranted(true)
                // Read steps if permissions are granted
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
            } else {
                permissionsGranted(false)
                return null
            }
        } catch (e: Exception) {
            // Handle exceptions like permission not granted, API not available, etc.
            permissionsGranted(false)
            e.printStackTrace()
            return null
        }
    }
}
