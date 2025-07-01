package com.stepblocks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.google.android.gms.wearable.Wearable
import com.stepblocks.ui.theme.StepBlocksTheme
import com.stepblocks.workers.StepCacheWorker
import com.stepblocks.workers.StepPruningWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
        GlobalScope.launch {
            val nodes = Wearable.getNodeClient(this@MainActivity).connectedNodes.await()
            for (node in nodes) {
                Wearable.getMessageClient(this@MainActivity)
                    .sendMessage(node.id, "/test", "hello".toByteArray())
            }
        }
        val serviceIntent = Intent(this, StepTrackingService::class.java)
        startForegroundService(serviceIntent)
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
        // Start tracking steps
        // This would typically involve setting up workers and sensors
        WorkManager.getInstance(applicationContext).also { workManager ->
            StepCacheWorker.enqueue(workManager)
            StepPruningWorker.enqueue(workManager)
        }
    }
}
