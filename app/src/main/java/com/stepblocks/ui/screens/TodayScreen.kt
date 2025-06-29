package com.stepblocks.ui.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.data.Template
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = viewModel(factory = TodayViewModelFactory(LocalContext.current.applicationContext as Application)),
    contentPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val context = LocalContext.current
    var showTemplateDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.getPermissionRequestContract(),
        onResult = viewModel::onPermissionsResult
    )

    // Observe lifecycle and load data when the screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadData()
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.permissionsGranted && !uiState.showPermissionRationale) {
            requestPermissionLauncher.launch(viewModel.healthConnectManager.permissions)
        }
    }

    if (showTemplateDialog) {
        TemplateSelectionDialog(
            templates = templates,
            onDismiss = { showTemplateDialog = false },
            onTemplateSelected = { templateId ->
                viewModel.assignTemplateToToday(templateId)
                showTemplateDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Changed to Top for better layout control
    ) {


        AnimatedVisibility(
            visible = !uiState.permissionsGranted && uiState.showPermissionRationale,
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
                Button(onClick = {
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
            visible = uiState.permissionsGranted && uiState.showNoTemplateMessage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No template set for today.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { showTemplateDialog = true }) {
                    Text("Select one")
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.permissionsGranted && !uiState.showNoTemplateMessage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Template for today
                Text(
                    text = "Template for today: ${uiState.currentTemplate?.name ?: "-"}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Daily Progress Indicator
                val dailyProgress = if (uiState.dailyTarget > 0) uiState.totalDailySteps.toFloat() / uiState.dailyTarget else 0f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    CircularProgressIndicator(
                        progress = dailyProgress,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = MaterialTheme.colorScheme.primary, // Current color for progress
                        trackColor = MaterialTheme.colorScheme.surfaceVariant // Muted base color
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.totalDailySteps}",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "/ ${uiState.dailyTarget} steps",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Current Block: Name
                Text(
                    text = "Current block: ${uiState.currentBlockName}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Current Block Progress Indicator
                val blockProgress = if (uiState.currentBlockTarget > 0) uiState.currentBlockSteps.toFloat() / uiState.currentBlockTarget else 0f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ) {
                    CircularProgressIndicator(
                        progress = blockProgress,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 10.dp,
                        color = MaterialTheme.colorScheme.secondary, // Current color for progress
                        trackColor = MaterialTheme.colorScheme.surfaceVariant // Muted base color
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.currentBlockSteps}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "/ ${uiState.currentBlockTarget} steps",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time remaining in block
                Text(
                    text = "${uiState.timeRemainingInBlock} in current block",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun TemplateSelectionDialog(
    templates: List<Template>,
    onDismiss: () -> Unit,
    onTemplateSelected: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select a Template", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(templates) { template ->
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTemplateSelected(template.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}