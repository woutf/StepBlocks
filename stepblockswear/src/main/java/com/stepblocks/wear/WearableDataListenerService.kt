package com.stepblocks.wear

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.stepblocks.wear.data.StepDao
import com.stepblocks.wear.data.StepDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "WatchWDS"

class WearableDataListenerService : WearableListenerService() {

    private lateinit var stepDao: StepDao
    private lateinit var serviceScope: CoroutineScope
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearableDataListenerService onCreate called (Wear)")
        stepDao = StepDatabase.getDatabase(applicationContext).stepDao()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        Log.d(TAG, "Service created (onCreate)")
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged (TOP) (Wear)")
        super.onDataChanged(dataEvents)
        Log.d(TAG, "onDataChanged: ${dataEvents.count()} events (entry)")
        dataEvents.forEach { event ->
            val path = event.dataItem.uri.path
            Log.d(TAG, "Data event: type=${event.type}, path=$path, uri=${event.dataItem.uri}")
            if (path == "/request_historical_data") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val lastKnownTime = dataMap.getLong("last_known_time")
                Log.d(TAG, "Received historical data request since: $lastKnownTime (onDataChanged)")
                serviceScope.launch {
                    val historicalData = stepDao.getUnsyncedData(false).filter { it.timestamp > lastKnownTime }
                    Log.d(TAG, "Found ${historicalData.size} historical records to send (onDataChanged)")
                    val dataMapArrayList = ArrayList<DataMap>()
                    historicalData.forEach {
                        val data = DataMap()
                        data.putLong("step_delta", it.stepDelta)
                        data.putLong("timestamp", it.timestamp)
                        dataMapArrayList.add(data)
                        Log.d(TAG, "Prepared historical record: stepDelta=${it.stepDelta}, timestamp=${it.timestamp}")
                    }
                    val requestData = com.google.android.gms.wearable.PutDataMapRequest.create("/historical_data_response").apply {
                        dataMap.putDataMapArrayList("historical_data", dataMapArrayList)
                    }.asPutDataRequest()
                    try {
                        dataClient.putDataItem(requestData).await()
                        Log.d(TAG, "Historical data response sent successfully (onDataChanged)")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send historical data response (onDataChanged)", e)
                    }
                }
            } else {
                Log.d(TAG, "Unknown data path: $path (onDataChanged)")
            }
        }
        Log.d(TAG, "onDataChanged: (exit)")
    }

    override fun onPeerConnected(peer: Node) {
        Log.d(TAG, "onPeerConnected (TOP) (Wear)")
        super.onPeerConnected(peer)
        Log.d(TAG, "Peer connected: ${peer.displayName} (onPeerConnected)")
        serviceScope.launch {
            try {
                messageClient.sendMessage(peer.id, "/peer_connected", null).await()
                Log.d(TAG, "Sent /peer_connected message to ${peer.displayName} (onPeerConnected)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send /peer_connected message (onPeerConnected)", e)
            }
        }
    }

    override fun onPeerDisconnected(peer: Node) {
        Log.d(TAG, "onPeerDisconnected (TOP) (Wear)")
        super.onPeerDisconnected(peer)
        Log.d(TAG, "Peer disconnected: ${peer.displayName} (onPeerDisconnected)")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived (TOP) (Wear)")
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Message received: ${messageEvent.path} (onMessageReceived)")
        if (messageEvent.path == "/test") {
            Log.d(TAG, "Received test message from wear: ${String(messageEvent.data)}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed (onDestroy)")
    }
}
