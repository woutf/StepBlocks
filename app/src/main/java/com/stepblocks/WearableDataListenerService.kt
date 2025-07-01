package com.stepblocks

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
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

private const val TAG = "PhoneWDS"

class WearableDataListenerService : WearableListenerService() {

    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var serviceScope: CoroutineScope
    private val dataClient by lazy { Wearable.getDataClient(this) }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearableDataListenerService onCreate called")
        Log.d(TAG, "Service created (onCreate)")
        healthConnectRepository = HealthConnectRepository.getInstance(applicationContext)
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged (TOP)")
        super.onDataChanged(dataEvents)
        Log.d(TAG, "onDataChanged: ${dataEvents.count()} events (entry)")
        dataEvents.forEach { event ->
            val path = event.dataItem.uri.path
            Log.d(TAG, "Data event: type=${event.type}, path=$path, uri=${event.dataItem.uri}")
            if (event.type == DataEvent.TYPE_CHANGED) {
                when (path) {
                    "/step_update" -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val stepDelta = dataMap.getInt("step_delta", 0)
                        Log.d(TAG, "Received step update: $stepDelta (onDataChanged)")
                        serviceScope.launch {
                            Log.d(TAG, "[SYNC] Setting status to Syncing")
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Syncing)
                            val now = Instant.now()
                            Log.d(TAG, "[SYNC] Syncing $stepDelta steps to HealthConnect at $now")
                            healthConnectRepository.syncStepsToHealthConnect(stepDelta, now, now)
                            healthConnectRepository.updateRealtimeSteps(healthConnectRepository.realtimeSteps.value + stepDelta)
                            Log.d(TAG, "[SYNC] Setting status to Connected")
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
                        }
                    }
                    "/historical_data_response" -> {
                        Log.d(TAG, "Received historical data response (onDataChanged)")
                        serviceScope.launch {
                            Log.d(TAG, "[SYNC] Setting status to Syncing (historical)")
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Syncing)
                            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                            val historicalData = dataMap.getDataMapArrayList("historical_data")
                            Log.d(TAG, "Historical data count: ${historicalData?.size}")
                            historicalData?.forEach {
                                val stepDelta = it.getInt("step_delta")
                                val timestamp = it.getLong("timestamp")
                                Log.d(TAG, "[SYNC] Syncing historical stepDelta=$stepDelta at timestamp=$timestamp")
                                healthConnectRepository.syncStepsToHealthConnect(stepDelta, Instant.ofEpochMilli(timestamp), Instant.ofEpochMilli(timestamp))
                            }
                            Log.d(TAG, "[SYNC] Setting status to Connected (historical)")
                            healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
                        }
                    }
                    else -> {
                        Log.d(TAG, "Unknown data path: $path (onDataChanged)")
                    }
                }
            }
        }
        Log.d(TAG, "onDataChanged: (exit)")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/test") {
            Log.d(TAG, "Received test message from wear: ${String(messageEvent.data)}")
        }
        Log.d(TAG, "onMessageReceived (TOP)")
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Message received: ${messageEvent.path} (onMessageReceived)")
        if (messageEvent.path == "/peer_connected") {
            serviceScope.launch {
                Log.d(TAG, "[CONN] Setting status to Connected (peer_connected message)")
                healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
                val lastKnownTime = healthConnectRepository.getLastKnownTime()
                Log.d(TAG, "Requesting historical data since: $lastKnownTime (onMessageReceived)")
                val requestData = com.google.android.gms.wearable.PutDataMapRequest.create("/request_historical_data").apply {
                    dataMap.putLong("last_known_time", lastKnownTime.toEpochMilli())
                }.asPutDataRequest()
                try {
                    dataClient.putDataItem(requestData).await()
                    Log.d(TAG, "Historical data request sent successfully (onMessageReceived)")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send historical data request (onMessageReceived)", e)
                }
            }
        } else {
            Log.d(TAG, "Unknown message path: ${messageEvent.path} (onMessageReceived)")
        }
    }

    override fun onPeerConnected(peer: Node) {
        Log.d(TAG, "onPeerConnected (TOP)")
        super.onPeerConnected(peer)
        Log.d(TAG, "Peer connected: ${peer.displayName} (onPeerConnected)")
    }

    override fun onPeerDisconnected(peer: Node) {
        Log.d(TAG, "onPeerDisconnected (TOP)")
        super.onPeerDisconnected(peer)
        Log.d(TAG, "Peer disconnected: ${peer.displayName} (onPeerDisconnected)")
        healthConnectRepository.updateConnectionStatus(ConnectionStatus.Disconnected)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed (onDestroy)")
    }
}
