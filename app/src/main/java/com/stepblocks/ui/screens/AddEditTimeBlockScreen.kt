package com.stepblocks.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.stepblocks.viewmodel.AddEditTimeBlockViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private enum class PickerDialog {
    NONE, START_TIME, END_TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimeBlockScreen(
    navController: NavController,
    viewModel: AddEditTimeBlockViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    var dialogToShow by remember { mutableStateOf(PickerDialog.NONE) }

    if (dialogToShow != PickerDialog.NONE) {
        val isStartTimePicker = dialogToShow == PickerDialog.START_TIME
        val timeToEdit = if (isStartTimePicker) uiState.startTime else uiState.endTime
        val currentTime = try {
            LocalTime.parse(timeToEdit, formatter)
        } catch (e: Exception) {
            LocalTime.now()
        }

        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = {
                dialogToShow = PickerDialog.NONE
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute).format(formatter)
                        if (isStartTimePicker) {
                            viewModel.onStartTimeChange(newTime)
                        } else {
                            viewModel.onEndTimeChange(newTime)
                        }
                        dialogToShow = PickerDialog.NONE
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogToShow = PickerDialog.NONE
                    }
                ) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    LaunchedEffect(uiState.isTimeBlockSaved) {
        if (uiState.isTimeBlockSaved) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Time Block" else "Add Time Block") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Enable the button only if name is not blank and no validation errors
                    val isSaveEnabled = uiState.name.isNotBlank() && uiState.targetStepsError == null && uiState.timeRangeError == null && uiState.overlapError == null
                    TextButton(onClick = { viewModel.saveTimeBlock() }, enabled = isSaveEnabled) {
                        Text("Done")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Time Block Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Box {
                OutlinedTextField(
                    value = uiState.startTime,
                    onValueChange = { /* Read-only */ },
                    label = { Text("Start Time") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = uiState.timeRangeError != null || uiState.overlapError != null // Indicate error visually
                )
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        dialogToShow = PickerDialog.START_TIME
                    })
            }
            Box {
                OutlinedTextField(
                    value = uiState.endTime,
                    onValueChange = { /* Read-only */ },
                    label = { Text("End Time") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = uiState.timeRangeError != null || uiState.overlapError != null // Indicate error visually
                )
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        dialogToShow = PickerDialog.END_TIME
                    })
            }
            // Display time range error message if present
            uiState.timeRangeError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
            // Display overlap error message if present
            uiState.overlapError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
            OutlinedTextField(
                value = uiState.targetSteps,
                onValueChange = { viewModel.onTargetStepsChange(it) },
                label = { Text("Target Steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.targetStepsError != null, // Set isError based on ViewModel state
                supportingText = { // Display error message
                    uiState.targetStepsError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Select time") },
        text = { content() },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    )
}
