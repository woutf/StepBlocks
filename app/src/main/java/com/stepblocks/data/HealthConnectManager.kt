package com.stepblocks.data

//import android.content.Context
//import androidx.health.connect.client.HealthConnectClient
//import androidx.health.connect.client.records.StepsRecord
//import androidx.health.connect.client.request.ReadRecordsRequest
//import androidx.health.connect.client.time.TimeRangeFilter
//import java.time.Instant
//
//class HealthConnectManager(private val context: Context) {
//
//    private val healthConnectClient by lazy {
//        HealthConnectClient.getOrCreate(context)
//    }
//
//    /**
//     * Reads the total step count from Health Connect for a given time range.
//     *
//     * @param startTime The start of the time range (inclusive).
//     * @param endTime The end of the time range (exclusive).
//     * @return The total step count as a Long. Returns 0 if no data or permissions are denied.
//     */
//    suspend fun getSteps(startTime: Instant, endTime: Instant): Long {
//        return try {
//            val response = healthConnectClient.readRecords(
//                ReadRecordsRequest(
//                    recordType = StepsRecord::class,
//                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
//                )
//            )
//            response.records.sumOf { it.count }
//        } catch (e: Exception) {
//            // TODO: Implement more robust error handling, e.g., permission checks, user notification
//            println("Error reading steps from Health Connect: ${e.message}")
//            0L // Return 0 if there's an error (e.g., permissions denied, no data)
//        }
//    }
//}
