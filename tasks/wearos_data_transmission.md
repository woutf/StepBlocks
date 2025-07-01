# Task: Intelligent Data Transmission

**Requirement:** FR1.2 - The app must transmit the step delta (new steps since the last send) to the phone app.
*   **Normal Mode:** Send an update when the unsynced step delta reaches 250 steps.
*   **Power Saver Mode:** Change the threshold to 1000 steps when the watch battery is below 20%.

---

## Work Done

The `StepTrackingService.kt` has been updated to implement the intelligent data transmission logic. This includes:
*   Maintaining an `unsyncedStepCount`.
*   Fetching the battery level to determine the `stepThreshold`.
*   Sending a `PutDataMapRequest` to the phone when the `unsyncedStepCount` reaches the `stepThreshold`.
*   Resetting `unsyncedStepCount` after a successful transmission.

### Changes in `StepTrackingService.kt`

```kotlin
package com.stepblocks.wear.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest // Added this import
import java.util.concurrent.Executor // Added this import

class StepTrackingService : Service() {

    private val TAG = "StepTrackingService"

    private val healthServicesClient by lazy { HealthServices.getClient(this) }
    private val passiveMonitoringClient by lazy { healthServicesClient.passiveMonitoringClient }
    private val dataClient: DataClient by lazy { Wearable.getDataClient(this) }
    private val batteryManager by lazy { getSystemService(Context.BATTERY_SERVICE) as BatteryManager }

    private lateinit var mainExecutor: Executor

    private var unsyncedStepCount: Long = 0

    private val passiveListenerCallback = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            dataPoints.getData(DataType.STEPS_DAILY).forEach { dataPoint ->
                val steps = dataPoint.value
                unsyncedStepCount += steps

                val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val stepThreshold = if (batteryLevel < 20) 1000L else 250L

                if (unsyncedStepCount >= stepThreshold) {
                    sendStepUpdateToPhone(unsyncedStepCount)
                    unsyncedStepCount = 0
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mainExecutor = ContextCompat.getMainExecutor(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.STEPS_DAILY))
            .build()

        passiveMonitoringClient.setPassiveListenerCallback(
            config,
            passiveListenerCallback
        )
        
        return START_STICKY
    }

    private fun sendStepUpdateToPhone(stepDelta: Long) {
        val putDataMapRequest = PutDataMapRequest.create("/step_update").apply { // Changed PutDataRequest to PutDataMapRequest
            dataMap.putLong("step_delta", stepDelta)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest()
        val task = dataClient.putDataItem(putDataRequest)

        task.addOnSuccessListener { dataItem ->
            Log.d(TAG, "Sent step update to phone: ${dataItem.uri}")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to send step update to phone", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
```
