package com.stepblocks.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    /**
     * Reads the total step count from Health Connect for a given time range.
     *
     * @param startTime The start of the time range (inclusive).
     * @param endTime The end of the time range (exclusive).
     * @return The total step count as a Long, or null if permissions are denied or an error occurs.
     */
    suspend fun readSteps(startTime: Instant, endTime: Instant): Long? {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.count }
        } catch (e: Exception) {
            // TODO: Differentiate between permission errors and other exceptions.
            // For now, any exception is treated as a failure to read steps.
            println("Error reading steps from Health Connect: ${e.message}")
            null
        }
    }
}
