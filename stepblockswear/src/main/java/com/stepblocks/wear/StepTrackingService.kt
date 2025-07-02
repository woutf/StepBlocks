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
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StepTrackingService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var healthServicesClient: HealthServicesClient
    private lateinit var passiveListenerCallback: PassiveListenerCallback

    private var lastSentSteps = 0L
    private val STEP_UPDATE_THRESHOLD = 250L

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

    override fun onBind(intent: Intent?): IBinder? = null

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
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
    }

    private fun createPassiveMonitoringCallback() {
        passiveListenerCallback = object : PassiveListenerCallback {
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                val stepsDataPoint = dataPoints.getData(DataType.STEPS)
                if (stepsDataPoint.isNotEmpty()) {
                    val steps = stepsDataPoint.last().value
                    _currentSteps.value = steps
                    if (steps - lastSentSteps >= STEP_UPDATE_THRESHOLD) {
                        lastSentSteps = steps
                        sendStepsToPhone(steps)
                    }
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
                healthServicesClient.passiveMonitoringClient
                    .setPassiveListenerCallback(config, passiveListenerCallback)
            } catch (e: Exception) {
                // Handle exceptions, e.g., permissions not granted
            }
        }
    }

    private fun sendStepsToPhone(steps: Long) {
        serviceScope.launch {
            try {
                val nodes = Wearable.getNodeClient(this@StepTrackingService).connectedNodes.await()
                for (node in nodes) {
                    val stepsBytes = steps.toString().toByteArray()
                    Wearable.getMessageClient(this@StepTrackingService)
                        .sendMessage(node.id, "/steps_update", stepsBytes)
                        .await()
                }
            } catch (e: Exception) {
                // Optionally log or handle error
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "step_tracking_channel"
        val currentSteps = MutableStateFlow(0L) // For UI observation
    }
}
