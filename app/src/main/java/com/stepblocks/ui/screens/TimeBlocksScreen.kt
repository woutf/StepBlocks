package com.stepblocks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.data.TimeBlock
import com.stepblocks.viewmodel.ITimeBlocksViewModel
import com.stepblocks.viewmodel.TimeBlocksViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlocksScreen(
    viewModel: ITimeBlocksViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val timeBlocks by viewModel.timeBlocks.collectAsState()
    val assignedDays by viewModel.assignedDays.collectAsState()
    val editableTemplateName by viewModel.editableTemplateName.collectAsState()
    val showNoTimeBlocksError by viewModel.showNoTimeBlocksError.collectAsState() // Observe error state
    val focusManager = LocalFocusManager.current
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var timeBlockToDelete by remember { mutableStateOf<TimeBlock?>(null) }
    val totalSteps = remember(timeBlocks) {
        timeBlocks.sumOf { it.targetSteps }
    }

    var showEditTitleDialog by remember { mutableStateOf(false) } 

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                timeBlockToDelete = null
            },
            title = { Text("Delete Time Block") },
            text = { Text("Are you sure you want to delete '${timeBlockToDelete?.name}'?") },
            confirmButton = {
                Button(onClick = {
                    timeBlockToDelete?.let { viewModel.deleteTimeBlock(it) }
                    showDeleteConfirmation = false
                    timeBlockToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmation = false
                    timeBlockToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditTitleDialog) {
        var dialogTemplateName by remember { mutableStateOf(editableTemplateName) }
        AlertDialog(
            onDismissRequest = { showEditTitleDialog = false },
            title = { Text("Edit Template Name") },
            text = {
                OutlinedTextField(
                    value = dialogTemplateName,
                    onValueChange = { dialogTemplateName = it },
                    label = { Text("Template Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTemplateName(dialogTemplateName)
                    showEditTitleDialog = false
                    focusManager.clearFocus()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        editableTemplateName,
                        modifier = Modifier.clickable { showEditTitleDialog = true } 
                    )
                },
                actions = {
                    Text(text = "Total: $totalSteps steps")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            // FAB removed as per user request
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            // Display error message if no time blocks
            if (showNoTimeBlocksError) {
                item {
                    Text(
                        text = "This template must have at least one time block.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            // REMOVED INLINE TEMPLATE NAME EDITING UI
            items(timeBlocks) { timeBlock ->
                TimeBlockCard(
                    timeBlock = timeBlock,
                    onEdit = { onNavigateToEdit(timeBlock.id) },
                    onDelete = {
                        timeBlockToDelete = timeBlock
                        showDeleteConfirmation = true
                    }
                )
            }
            item { // ADD TIME BLOCK BUTTON REMAINS HERE
                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Time Block")
                        Text("Add Time Block")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Assign to Days",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    val dayMapping = listOf(1, 2, 3, 4, 5, 6, 0) // Mon=1, Tue=2,..., Sun=0

                    for (i in 0..6) {
                        val dayOfWeekAppIndex = dayMapping[i]
                        val isAssigned = assignedDays.contains(dayOfWeekAppIndex)

                        OutlinedButton(
                            onClick = { viewModel.toggleDayAssignment(dayOfWeekAppIndex) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, if (isAssigned) Color.Transparent else MaterialTheme.colorScheme.outline),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isAssigned) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isAssigned) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(dayLabels[i])
                        }
                    }
                }
            }
        }
    }
}


// Preview using a fake ViewModel that implements the interface
class FakeTimeBlocksViewModel(
    fakeTemplateWithTimeBlocks: TemplateWithTimeBlocks?
) : ITimeBlocksViewModel {
    private val _templateWithTimeBlocks = MutableStateFlow(fakeTemplateWithTimeBlocks)
    override val templateWithTimeBlocks: StateFlow<TemplateWithTimeBlocks?> = _templateWithTimeBlocks

    private val _timeBlocks = MutableStateFlow(fakeTemplateWithTimeBlocks?.timeBlocks ?: emptyList())
    override val timeBlocks: StateFlow<List<TimeBlock>> = _timeBlocks

    private val _assignedDays = MutableStateFlow(setOf<Int>(0, 1)) // Dummy assigned days: Sunday, Monday
    override val assignedDays: StateFlow<Set<Int>> = _assignedDays

    private val _editableTemplateName = MutableStateFlow(fakeTemplateWithTimeBlocks?.template?.name ?: "")
    override val editableTemplateName: StateFlow<String> = _editableTemplateName

    override val showNoTimeBlocksError: StateFlow<Boolean> = MutableStateFlow(false) // Added for preview

    override fun deleteTimeBlock(timeBlock: TimeBlock) {
        val currentTemplateWithTimeBlocks = _templateWithTimeBlocks.value
        if (currentTemplateWithTimeBlocks != null) {
            val updatedBlocks = currentTemplateWithTimeBlocks.timeBlocks.filterNot { it.id == timeBlock.id }
            _templateWithTimeBlocks.value = currentTemplateWithTimeBlocks.copy(timeBlocks = updatedBlocks)
            _timeBlocks.value = updatedBlocks
        }
    }

    override fun toggleDayAssignment(dayOfWeek: Int) {
        _assignedDays.update { currentDays ->
            if (currentDays.contains(dayOfWeek)) {
                currentDays - dayOfWeek
            } else {
                currentDays + dayOfWeek
            }
        }
    }

    override fun updateTemplateName(newName: String) {
        val currentTemplate = _templateWithTimeBlocks.value?.template
        if (currentTemplate != null) {
            _editableTemplateName.value = newName
            // In a real app, you'd trigger a repository update here.
            // For preview, we're just updating the local state.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeBlocksScreenPreview() {
    val dummyTimeBlocks = listOf(
        TimeBlock(1, 1L, "Morning Stroll", LocalTime.of(8, 0), LocalTime.of(9, 0), 1500, true, true, false),
        TimeBlock(2, 1L, "Lunch Break Walk", LocalTime.of(12, 30), LocalTime.of(13, 0), 500, false, true, false),
        TimeBlock(3, 1L, "Evening Power Walk", LocalTime.of(18, 0), LocalTime.of(18, 30), 2000, true, true, true)
    )
    val dummyTemplate = Template(1, "Weekday")
    val dummyTemplateWithTimeBlocks = TemplateWithTimeBlocks(dummyTemplate, dummyTimeBlocks)
    val fakeViewModel = FakeTimeBlocksViewModel(dummyTemplateWithTimeBlocks)
    TimeBlocksScreen(
        viewModel = fakeViewModel,
        onNavigateToAdd = {},
        onNavigateToEdit = {}
    )
}
