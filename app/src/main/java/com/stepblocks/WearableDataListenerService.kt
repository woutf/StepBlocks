package com.stepblocks

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.stepblocks.repository.HealthConnectRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

class WearableDataListenerService : WearableListenerService() {

    private lateinit var healthConnectRepository: HealthConnectRepository

    override fun onCreate() {
        super.onCreate()
        healthConnectRepository = HealthConnectRepository(applicationContext)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/step_update") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val stepDelta = dataMap.getInt("step_delta", 0)
                    
                    // Update UI via ViewModel/Repository (TODO)
                    // Queue for Health Connect Sync
                    GlobalScope.launch {
                        val now = Instant.now()
                        healthConnectRepository.syncStepsToHealthConnect(stepDelta, now, now)
                    }
                }
            }
        }
    }
}
