package com.stepblocks.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stepblocks.viewmodel.AddEditTimeBlockViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private enum class PickerDialog {
    NONE, START_TIME, END_TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimeBlockScreen(
    viewModel: AddEditTimeBlockViewModel,
    contentPadding: PaddingValues,
    onSave: () -> Unit
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

    // Enable the save action only if name is not blank and no validation errors
    val isSaveEnabled = uiState.name.isNotBlank() && uiState.targetStepsError == null && uiState.timeRangeError == null && uiState.overlapError == null

    LaunchedEffect(uiState.isTimeBlockSaved) {
        if (uiState.isTimeBlockSaved) {
            onSave() // Call the onSave callback when time block is saved
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
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
                isError = uiState.timeRangeError != null || uiState.overlapError != null
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
                isError = uiState.timeRangeError != null || uiState.overlapError != null
            )
            Box(modifier = Modifier
                .matchParentSize()
                .clickable {
                    dialogToShow = PickerDialog.END_TIME
                })
        }
        uiState.timeRangeError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        }
        uiState.overlapError?.let {
            Text(
                text = it,
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
            isError = uiState.targetStepsError != null,
            supportingText = {
                uiState.targetStepsError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )
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