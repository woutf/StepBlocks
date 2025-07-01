package com.stepblocks.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import java.time.Instant
import java.time.ZoneOffset

class HealthConnectRepository(private val context: Context) {

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun syncStepsToHealthConnect(stepDelta: Int, startTime: Instant, endTime: Instant) {
        val recordId = "stepblocks-steps-${startTime.toEpochMilli()}"

        val stepsRecord = StepsRecord(
            count = stepDelta.toLong(),
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime),
            endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endTime),
            metadata = Metadata.Builder()
                .setDevice(Device.Builder().setManufacturer("StepBlocks").setModel("Pixel Watch").setType(Device.TYPE_WATCH).build())
                .setClientRecordId(recordId)
                .setClientRecordVersion(1)
                .build()
        )

        try {
            healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            // TODO: Implement retry logic with exponential backoff
        }
    }
}
