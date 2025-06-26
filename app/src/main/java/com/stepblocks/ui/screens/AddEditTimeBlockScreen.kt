package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stepblocks.viewmodel.AddEditTimeBlockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimeBlockScreen(
    navController: NavController,
    viewModel: AddEditTimeBlockViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
                onValueChange = { viewModel.onStartTimeChange(it) },
                label = { Text("Start Time (e.g., 09:00)") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = uiState.endTime,
                onValueChange = { viewModel.onEndTimeChange(it) },
                label = { Text("End Time (e.g., 10:00)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
