package com.stepblocks.wear

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
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

class WearableDataListenerService : WearableListenerService() {

    private lateinit var stepDao: StepDao
    private lateinit var serviceScope: CoroutineScope
    private val dataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate() {
        super.onCreate()
        stepDao = StepDatabase.getDatabase(applicationContext).stepDao()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            val path = event.dataItem.uri.path
            if (path == "/request_historical_data") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val lastKnownTime = dataMap.getLong("last_known_time")
                serviceScope.launch {
                    val historicalData = stepDao.getUnsyncedData(false).filter { it.timestamp > lastKnownTime }
                    val dataMapArrayList = ArrayList<DataMap>()
                    historicalData.forEach {
                        val data = DataMap()
                        data.putInt("step_delta", it.stepDelta)
                        data.putLong("timestamp", it.timestamp)
                        dataMapArrayList.add(data)
                    }
                    val requestData = com.google.android.gms.wearable.PutDataMapRequest.create("/historical_data_response").apply {
                        dataMap.putDataMapArrayList("historical_data", dataMapArrayList)
                    }.asPutDataRequest()
                    try {
                        dataClient.putDataItem(requestData).await()
                    } catch (e: Exception) {
                        // Handle exception
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
