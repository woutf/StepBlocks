package com.stepblocks.ui.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.data.HealthConnectManager

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = viewModel(factory = TodayViewModelFactory(LocalContext.current.applicationContext as Application)),
    contentPadding: androidx.compose.foundation.layout.PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.getPermissionRequestContract(),
        onResult = viewModel::onPermissionsResult
    )

    // Check and request permissions on launch if not granted
    LaunchedEffect(Unit) {
        if (!uiState.permissionsGranted && !uiState.showPermissionRationale) {
            requestPermissionLauncher.launch(viewModel.healthConnectManager.permissions)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && !uiState.permissionsGranted && uiState.showPermissionRationale,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Health Connect permissions required to track steps.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { requestPermissionLauncher.launch(viewModel.healthConnectManager.permissions) }) {
                    Text("Grant Permissions")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Navigate to app settings */
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open App Settings")
                }
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.permissionsGranted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Total Daily Steps: ${uiState.totalDailySteps} / ${uiState.dailyTarget}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current Block: ${uiState.currentBlockName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Block Steps: ${uiState.currentBlockSteps} / ${uiState.currentBlockTarget}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Time Remaining: ${uiState.timeRemainingInBlock}",
                    style = MaterialTheme.typography.bodyMedium
                )
                // TODO: Add circular progress indicators
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodayScreenPreview() {
    val fakeApplication = Application() // This won't work correctly in a real preview
    val healthConnectManager = HealthConnectManager(fakeApplication.applicationContext)
    TodayScreen(
        viewModel = TodayViewModel(
            application = fakeApplication,
            healthConnectManager = healthConnectManager
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    )
}
