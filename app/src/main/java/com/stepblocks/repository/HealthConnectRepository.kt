package com.stepblocks.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class HealthConnectRepository(private val context: Context) {

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun getLastKnownTime(): Instant {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now()
            )
        )
        val response = healthConnectClient.readRecords(request)
        return response.records.maxOfOrNull { it.endTime } ?: Instant.now().minus(1, ChronoUnit.DAYS)
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
            healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            // TODO: Implement retry logic with exponential backoff
        }
    }
}
