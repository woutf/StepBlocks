package com.stepblocks.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.stepblocks.viewmodel.AddEditTimeBlockViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimeBlockScreenWithPicker(
    navController: NavController,
    viewModel: AddEditTimeBlockViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isTimeBlockSaved) {
        if (uiState.isTimeBlockSaved) {
            navController.navigateUp()
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeSet = { hour, minute ->
                viewModel.onStartTimeChange(String.format("%02d:%02d", hour, minute))
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeSet = { hour, minute ->
                viewModel.onEndTimeChange(String.format("%02d:%02d", hour, minute))
                showEndTimePicker = false
            }
        )
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
                    IconButton(onClick = { viewModel.saveTimeBlock() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save Time Block")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Time Block Name") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = uiState.startTime,
                onValueChange = {},
                label = { Text("Start Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartTimePicker = true },
                readOnly = true,
                enabled = false
            )
            TextField(
                value = uiState.endTime,
                onValueChange = {},
                label = { Text("End Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEndTimePicker = true },
                readOnly = true,
                enabled = false
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSet: (hour: Int, minute: Int) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val dialog = TimePickerDialog(
        context,
        { _, h, m -> onTimeSet(h, m) },
        hour,
        minute,
        true
    )

    DisposableEffect(Unit) {
        dialog.show()
        onDispose {
            dialog.dismiss()
        }
    }
}
