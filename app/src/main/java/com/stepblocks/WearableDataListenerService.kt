package com.stepblocks

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import com.stepblocks.data.AppDatabase
import com.stepblocks.data.DailyProgress
import com.stepblocks.repository.ConnectionStatus
import com.stepblocks.repository.HealthConnectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

private const val TAG = "PhoneWDS"

class WearableDataListenerService : WearableListenerService() {

    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        healthConnectRepository = HealthConnectRepository.getInstance(applicationContext)
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // No longer used for step updates
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/steps_update") {
            val newTotal = String(messageEvent.data).toLongOrNull() ?: return
            serviceScope.launch {
                val today = LocalDate.now()
                val date = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val db = AppDatabase.getDatabase(applicationContext)
                val dailyProgressDao = db.dailyProgressDao()

                // Get or create today's DailyProgress
                val dailyProgress = dailyProgressDao.getDailyProgressByDate(date)
                    ?: DailyProgress(date = date, templateId = "", blockProgress = emptyList(), lastStepTotal = 0L)

                val lastTotal = dailyProgress.lastStepTotal
                val delta = (newTotal - lastTotal).coerceAtLeast(0L)

                if (delta > 0) {
                    val now = Instant.now()
                    healthConnectRepository.updateConnectionStatus(ConnectionStatus.Syncing)
                    healthConnectRepository.syncStepsToHealthConnect(delta.toInt(), now, now)
                    healthConnectRepository.updateRealtimeSteps(newTotal)
                    // Update Room with new total
                    dailyProgressDao.insertDailyProgress(
                        dailyProgress.copy(lastStepTotal = newTotal)
                    )
                    healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
                } else if (lastTotal == 0L) {
                    // First run or midnight reset
                    healthConnectRepository.updateRealtimeSteps(newTotal)
                    dailyProgressDao.insertDailyProgress(
                        dailyProgress.copy(lastStepTotal = newTotal)
                    )
                }
            }
        }
    }

    override fun onPeerConnected(peer: Node) {
        Log.d(TAG, "Peer connected: ${peer.displayName}")
        healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
    }

    override fun onPeerDisconnected(peer: Node) {
        Log.d(TAG, "Peer disconnected: ${peer.displayName}")
        healthConnectRepository.updateConnectionStatus(ConnectionStatus.Disconnected)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
