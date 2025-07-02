package com.stepblocks.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.wearable.Wearable

private const val TAG = "WatchWDS"

class WearableDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onPeerConnected(peer: Node) {
        Log.d(TAG, "Peer connected: ${peer.displayName}")
        // Send latest step count on reconnect
        val steps = StepTrackingService.currentSteps.value
        serviceScope.launch {
            try {
                Wearable.getMessageClient(this@WearableDataListenerService)
                    .sendMessage(peer.id, "/steps_update", steps.toString().toByteArray())
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send /steps_update message", e)
            }
        }
    }

    override fun onPeerDisconnected(peer: Node) {
        Log.d(TAG, "Peer disconnected: ${peer.displayName}")
        super.onPeerDisconnected(peer)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Message received: ${messageEvent.path}")
        super.onMessageReceived(messageEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}
