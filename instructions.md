<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

# Yes, no pseudo code, only real code

Below is a **step-by-step guide** with **real code** to refactor your codebase and implement the new, simplified step tracking and sync logic for your Pixel Watch app.

## 1. **Remove Unnecessary Files and References**

Delete these files:

- `AppDatabase.kt`[^1]
- `StepDatabase.kt`[^2]
- `CachedStepEntity.kt`[^3]
- `StepDao.kt`[^4]
- `StepCacheWorker.kt`[^5]
- `StepPruningWorker.kt`[^6]

Remove all references to these classes in:

- `WearApplication.kt`[^7]
- `MainActivity.kt`[^8]
- `WearableDataListenerService.kt`[^9]


## 2. **Update `WearApplication.kt`**

**Before:**
Contains worker scheduling and factory logic for step caching/pruning[^7].

**After:**
Remove all worker-related code. The file can now simply be:

```kotlin
package com.stepblocks

import android.app.Application

class WearApplication : Application()
```


## 3. **Update `MainActivity.kt`**

- Remove all references to `StepCacheWorker` and `StepPruningWorker` in `startTracking()` and elsewhere[^8].
- Ensure permissions and service start remain.

**Example:**

```kotlin
package com.stepblocks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.Wearable
import com.stepblocks.ui.theme.StepBlocksTheme
import com.stepblocks.wear.StepTrackingService

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.BODY_SENSORS
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startTracking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DEBUG", "MainActivity onCreate called")
        checkAndRequestPermissions()
        setContent {
            StepBlocksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        val serviceIntent = Intent(this, StepTrackingService::class.java)
        startForegroundService(serviceIntent)
    }
}
```


## 4. **Update `MainScreen.kt`**

- Replace placeholder text with a composable that shows the current step count, using a `StateFlow` or similar from `StepTrackingService`[^10].

**Example:**

```kotlin
package com.stepblocks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stepblocks.wear.StepTrackingService

@Composable
fun MainScreen() {
    // Replace with a real shared ViewModel or state holder in production
    val steps by StepTrackingService.currentSteps.collectAsState(initial = 0L)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "StepBlocks",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Steps: $steps",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```


## 5. **Update `StepTrackingService.kt`**

- Remove all TODOs about caching.
- Add logic to send the current daily step total to the phone every 250 steps using the Wear OS `MessageClient`[^11].

**Full Example:**

```kotlin
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
```


## 6. **Update `WearableDataListenerService.kt`**

- Remove all database and historical sync code[^9].
- Keep only peer connection and optional diagnostics.

**Example:**

```kotlin
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
        serviceScope.launch {
            try {
                Wearable.getMessageClient(this@WearableDataListenerService)
                    .sendMessage(peer.id, "/peer_connected", null)
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send /peer_connected message", e)
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
```

**With these changes, your watch app will:**

- Only track the current daily step total.
- Send that total to the phone every 250 steps.
- Show the current step count in the UI.
- Contain no local caching, historical data, or worker logic.

Let me know if you need a matching phone-side code sample or further refinements.

<div style="text-align: center">⁂</div>

[^1]: AppDatabase.kt

[^2]: StepDatabase.kt

[^3]: CachedStepEntity.kt

[^4]: StepDao.kt

[^5]: StepCacheWorker.kt

[^6]: StepPruningWorker.kt

[^7]: WearApplication.kt

[^8]: MainActivity.kt

[^9]: WearableDataListenerService.kt

[^10]: MainScreen.kt

[^11]: StepTrackingService.kt

