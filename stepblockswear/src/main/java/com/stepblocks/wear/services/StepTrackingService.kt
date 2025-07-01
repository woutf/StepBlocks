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
import java.util.concurrent.Executor

class StepTrackingService : Service() {

    private val healthServicesClient by lazy { HealthServices.getClient(this) }
    private val passiveMonitoringClient by lazy { healthServicesClient.passiveMonitoringClient }
    private lateinit var mainExecutor: Executor

    private val passiveListenerCallback = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            dataPoints.getData(DataType.STEPS_DAILY).forEach { dataPoint ->
                val steps = dataPoint.value
                // TODO: Handle step count
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
