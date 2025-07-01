package com.stepblocks

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearableDataListenerService : WearableListenerService() {
    // Inject ViewModel or Repository as needed

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/step_update") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val stepDelta = dataMap.getInt("step_delta", 0)
                    
                    // Update UI via ViewModel/Repository
                    // Queue for Health Connect Sync
                }
            }
        }
    }
}
