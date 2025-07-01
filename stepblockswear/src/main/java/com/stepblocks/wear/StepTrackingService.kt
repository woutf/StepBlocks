package com.stepblocks.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StepTrackingService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var healthServicesClient: HealthServicesClient
    private lateinit var passiveListenerCallback: PassiveListenerCallback

    private val _currentSteps = MutableStateFlow(0L)
    val currentSteps = _currentSteps.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        healthServicesClient = HealthServices.getClient(this)
        createNotificationChannel()
        createPassiveMonitoringCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification().build())
        subscribeToPassiveMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Step Tracking Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, this::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Tracking")
            .setContentText("Tracking your steps in the background")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Replace with a relevant icon
            .setContentIntent(pendingIntent)
    }

    private fun createPassiveMonitoringCallback() {
        passiveListenerCallback = object : PassiveListenerCallback {
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                val stepsDataPoint = dataPoints.getData(DataType.STEPS)
                if (stepsDataPoint.isNotEmpty()) {
                    val steps = stepsDataPoint.last().value
                    _currentSteps.value = steps
                    // TODO: Implement smart update & data transmission logic (FR1.2)
                    // TODO: Implement local caching (FR1.3)
                }
            }
        }
    }

    private fun subscribeToPassiveMonitoring() {
        serviceScope.launch {
            try {
                val config = PassiveListenerConfig.builder()
                    .setDataTypes(setOf(DataType.STEPS))
                    .build()
                healthServicesClient.passiveMonitoringClient.setPassiveListenerCallback(config, passiveListenerCallback)
            } catch (e: Exception) {
                // TODO: Handle exceptions, e.g., permissions not granted
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "step_tracking_channel"
    }
}