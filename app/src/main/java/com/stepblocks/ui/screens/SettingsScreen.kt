package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.viewmodel.SettingsUiState
import com.stepblocks.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.stepblocks.viewmodel.VibrationPattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Watch Connection Section
            Text(
                text = "Watch Connection",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = "Connection status: Connected", // Placeholder
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            OutlinedButton(
                onClick = { /* TODO: Implement sync now */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Sync Now")
            }
            Text(
                text = "Last sync: 2023-10-27 10:30 AM", // Placeholder
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Notifications Section
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progress updates")
                Switch(
                    checked = uiState.progressUpdatesEnabled,
                    onCheckedChange = { viewModel.onProgressUpdatesToggleChange(it) }
                )
            }
            if (uiState.progressUpdatesEnabled) {
                Column(modifier = Modifier.padding(start = 32.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("At start of time block")
                        Switch(
                            checked = uiState.beginBlockUpdates,
                            onCheckedChange = { viewModel.onBeginBlockUpdatesChange(it) }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Half way through a time block")
                        Switch(
                            checked = uiState.midBlockUpdates,
                            onCheckedChange = { viewModel.onMidBlockUpdatesChange(it) }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("At the end of time block")
                        Switch(
                            checked = uiState.endBlockUpdates,
                            onCheckedChange = { viewModel.onEndBlockUpdatesChange(it) }
                        )
                    }
                }
                VibrationPatternSelector(
                    label = "Behind Target",
                    selectedPattern = uiState.behindTargetPattern,
                    onPatternSelected = { viewModel.onBehindTargetPatternChange(it) }
                )
                VibrationPatternSelector(
                    label = "On Target",
                    selectedPattern = uiState.onTargetPattern,
                    onPatternSelected = { viewModel.onOnTargetPatternChange(it) }
                )
                VibrationPatternSelector(
                    label = "Ahead of Target",
                    selectedPattern = uiState.aheadTargetPattern,
                    onPatternSelected = { viewModel.onAheadTargetPatternChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Data Section
            Text(
                text = "Data",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Button(
                onClick = { /* TODO: Implement export history */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Export History")
            }
            Button(
                onClick = { /* TODO: Implement clear all data */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Add red color
            ) {
                Text("Clear All Data")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // About Section
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = "App version: 1.0.0", // Placeholder
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Help link (TODO)", // Placeholder
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibrationPatternSelector(
    label: String,
    selectedPattern: VibrationPattern,
    onPatternSelected: (VibrationPattern) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    fun formatPatternName(pattern: VibrationPattern): String {
        if (pattern == VibrationPattern.NONE) {
            return "No vibration"
        }
        return pattern.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp), // Indent to nest under the toggles
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = formatPatternName(selectedPattern),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    VibrationPattern.values().forEach { pattern ->
                        DropdownMenuItem(
                            text = { Text(formatPatternName(pattern)) },
                            onClick = {
                                onPatternSelected(pattern)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

class FakeSettingsViewModel(initialState: SettingsUiState) : SettingsViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<SettingsUiState> = _uiState

    override fun onProgressUpdatesToggleChange(isEnabled: Boolean) {
        _uiState.value = _uiState.value.copy(progressUpdatesEnabled = isEnabled)
    }

    override fun onBeginBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.value = _uiState.value.copy(beginBlockUpdates = isEnabled)
    }

    override fun onMidBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.value = _uiState.value.copy(midBlockUpdates = isEnabled)
    }

    override fun onEndBlockUpdatesChange(isEnabled: Boolean) {
        _uiState.value = _uiState.value.copy(endBlockUpdates = isEnabled)
    }

    override fun onBehindTargetPatternChange(pattern: VibrationPattern) {
        _uiState.value = _uiState.value.copy(behindTargetPattern = pattern)
    }

    override fun onOnTargetPatternChange(pattern: VibrationPattern) {
        _uiState.value = _uiState.value.copy(onTargetPattern = pattern)
    }

    override fun onAheadTargetPatternChange(pattern: VibrationPattern) {
        _uiState.value = _uiState.value.copy(aheadTargetPattern = pattern)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val fakeViewModel = FakeSettingsViewModel(initialState = SettingsUiState(
        progressUpdatesEnabled = true,
        beginBlockUpdates = true,
        midBlockUpdates = true,
        endBlockUpdates = true,
        behindTargetPattern = VibrationPattern.SHORT_DOUBLE_TAP,
        onTargetPattern = VibrationPattern.LONG_SINGLE,
        aheadTargetPattern = VibrationPattern.TRIPLE_TAP
    ))
    SettingsScreen(viewModel = fakeViewModel)
}
