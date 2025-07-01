package com.stepblocks.wear

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.data.ExerciseCapabilities
import androidx.health.services.client.data.ExerciseType
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import com.stepblocks.wear.services.StepTrackingService

class MainActivity : ComponentActivity() {

    private val healthServicesClient by lazy { HealthServices.getClient(this) }
    private val passiveMonitoringClient by lazy { healthServicesClient.passiveMonitoringClient }
    private var dailySteps by mutableStateOf(0L)
    private var isPermissionGranted by mutableStateOf(false)
    private var updateJob: Job? = null
    private var baselineSteps = 0L
    private var sessionStartSteps = 0L

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionGranted = isGranted
        if (isGranted) {
            startStepTracking()
        }
    }

    private val passiveListenerCallback = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            dataPoints.getData(DataType.STEPS_DAILY).forEach { dataPoint ->
                dailySteps = dataPoint.value
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissionAndStartTracking()

        setContent {
            StepCounterTheme {
                StepCounterScreen(
                    steps = dailySteps,
                    isPermissionGranted = isPermissionGranted,
                    onRequestPermission = { requestPermission() }
                )
            }
        }
    }

    private fun checkPermissionAndStartTracking() {
        isPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            startStepTracking()
        }
    }

    private fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun startStepTracking() {
        // Start the step tracking service
        val serviceIntent = Intent(this, StepTrackingService::class.java)
        startForegroundService(serviceIntent)

        // Start live step count updates
        startLiveStepUpdates()

        // Set up passive monitoring for both daily and regular step data
        lifecycleScope.launch {
            try {
                val config = PassiveListenerConfig.builder()
                    .setDataTypes(setOf(DataType.STEPS_DAILY, DataType.STEPS))
                    .build()

                passiveMonitoringClient.setPassiveListenerCallback(
                    config,
                    passiveListenerCallback
                )
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private fun startLiveStepUpdates() {
        updateJob?.cancel()
        updateJob = lifecycleScope.launch {
            // Get initial step count
            getInitialStepCount()

            while (true) {
                try {
                    // Query the step tracking service for current step count
                    StepTrackingService.unsyncedStepCount.collect { unsyncedSteps ->
                        if (unsyncedSteps > 0) {
                            dailySteps = baselineSteps + unsyncedSteps
                        }
                    }
                    delay(1000) // Update every second for real-time feel
                } catch (e: Exception) {
                    delay(2000) // Wait longer on error
                }
            }
        }
    }

    private fun getInitialStepCount() {
        val capabilitiesTask = passiveMonitoringClient.getCapabilitiesAsync()

        capabilitiesTask.addListener({
            try {
                if (capabilitiesTask.isDone) {
                    val capabilities = capabilitiesTask.get()
                    if (capabilities.supportedDataTypesPassiveMonitoring.contains(DataType.STEPS_DAILY)) {
                        if (dailySteps == 0L) {
                            dailySteps = 0L
                            baselineSteps = 0L
                        }
                    }
                }
            } catch (e: Exception) {
                println("Failed to get capabilities: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }



    private suspend fun getCurrentDailySteps() {
        // This method is handled by the live update loop and passive monitoring
    }

    private fun getTotalStepsFromCounter() {
        // This method is handled by the passive monitoring callback
    }

    override fun onResume() {
        super.onResume()
        if (isPermissionGranted) {
            startLiveStepUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        updateJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        // Clean up passive monitoring
        lifecycleScope.launch {
            try {
                passiveMonitoringClient.clearPassiveListenerCallbackAsync()
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
}

@Composable
fun StepCounterScreen(
    steps: Long,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isPermissionGranted) {
            StepCounterDisplay(steps = steps)
        } else {
            PermissionScreen(onRequestPermission = onRequestPermission)
        }
    }
}

@Composable
fun StepCounterDisplay(steps: Long) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        // Circular background for the step count
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E88E5)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%,d", steps).replace(",", "."),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "STEPS",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Live indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Green)
            )
        }
    }
}

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Activity Permission Required",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E88E5)
            )
        ) {
            Text(
                text = "Grant Permission",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StepCounterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF1E88E5),
            background = Color.Black,
            surface = Color.Black
        ),
        content = content
    )
}

@Preview
@Composable
fun StepCounterPreview() {
    StepCounterTheme {
        StepCounterScreen(
            steps = 8542,
            isPermissionGranted = true,
            onRequestPermission = {}
        )
    }
}