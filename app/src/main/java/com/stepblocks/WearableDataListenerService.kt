package com.stepblocks

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.stepblocks.repository.ConnectionStatus
import com.stepblocks.repository.HealthConnectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant

class WearableDataListenerService : WearableListenerService() {

    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var serviceScope: CoroutineScope
    private val dataClient by lazy { Wearable.getDataClient(this) }


    override fun onCreate() {
        super.onCreate()
        healthConnectRepository = HealthConnectRepository(applicationContext)
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                when (path) {
                    "/step_update" -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val stepDelta = dataMap.getInt("step_delta", 0)

                        serviceScope.launch {
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Syncing) // Set status to syncing during data transfer
                            val now = Instant.now()
                            healthConnectRepository.syncStepsToHealthConnect(stepDelta, now, now)
                            healthConnectRepository.updateRealtimeSteps(healthConnectRepository.realtimeSteps.value + stepDelta)
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected) // Back to connected after sync
                        }
                    }
                    "/historical_data_response" -> {
                        serviceScope.launch {
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Syncing)
                            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                            val historicalData = dataMap.getDataMapArrayList("historical_data")
                            historicalData?.forEach {
                                val stepDelta = it.getInt("step_delta")
                                val timestamp = it.getLong("timestamp")
                                healthConnectRepository.syncStepsToHealthConnect(stepDelta, Instant.ofEpochMilli(timestamp), Instant.ofEpochMilli(timestamp))
                            }
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
                        }
                    }
                }
            }
        }
    }

    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        serviceScope.launch {
            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
            val lastKnownTime = healthConnectRepository.getLastKnownTime()
            val requestData = com.google.android.gms.wearable.PutDataMapRequest.create("/request_historical_data").apply {
                dataMap.putLong("last_known_time", lastKnownTime.toEpochMilli())
            }.asPutDataRequest()

            try {
                dataClient.putDataItem(requestData).await()
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    override fun onPeerDisconnected(peer: Node) {
        super.onPeerDisconnected(peer)
        healthConnectRepository.updateConnectionStatus(ConnectionStatus.Disconnected)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
