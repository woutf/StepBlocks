package com.stepblocks.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.data.TimeBlock
import com.stepblocks.viewmodel.ITimeBlocksViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlocksScreen(
    viewModel: ITimeBlocksViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    contentPadding: PaddingValues,
    editableTemplateName: String,
    totalSteps: Int,
    showEditTemplateNameDialog: MutableState<Boolean>
) {
    val timeBlocks by viewModel.timeBlocks.collectAsState()
    val assignedDays by viewModel.timeBlocks.collectAsState() // This was wrong, should be viewModel.assignedDays
    val showNoTimeBlocksError by viewModel.showNoTimeBlocksError.collectAsState()
    val focusManager = LocalFocusManager.current

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var timeBlockToDelete by remember { mutableStateOf<TimeBlock?>(null) }

    var dialogTemplateName by remember { mutableStateOf(editableTemplateName) }

    LaunchedEffect(editableTemplateName) {
        dialogTemplateName = editableTemplateName
    }

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

    if (showEditTemplateNameDialog.value) {
        AlertDialog(
            onDismissRequest = { showEditTemplateNameDialog.value = false },
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
                    showEditTemplateNameDialog.value = false
                    focusManager.clearFocus()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTemplateNameDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(modifier = Modifier.padding(contentPadding)) {
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
        item {
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
    }
}


class FakeTimeBlocksViewModel(
    fakeTemplateWithTimeBlocks: TemplateWithTimeBlocks?
) : ITimeBlocksViewModel {
    private val _templateWithTimeBlocks = MutableStateFlow(fakeTemplateWithTimeBlocks)
    override val templateWithTimeBlocks: StateFlow<TemplateWithTimeBlocks?> = _templateWithTimeBlocks

    private val _timeBlocks = MutableStateFlow(fakeTemplateWithTimeBlocks?.timeBlocks ?: emptyList())
    override val timeBlocks: StateFlow<List<TimeBlock>> = _timeBlocks

    private val _assignedDays = MutableStateFlow(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
    override val assignedDays: StateFlow<Set<DayOfWeek>> = _assignedDays

    private val _editableTemplateName = MutableStateFlow(fakeTemplateWithTimeBlocks?.template?.name ?: "")
    override val editableTemplateName: StateFlow<String> = _editableTemplateName

    override val showNoTimeBlocksError: StateFlow<Boolean> = MutableStateFlow(false)

    override fun deleteTimeBlock(timeBlock: TimeBlock) {
        val currentTemplateWithTimeBlocks = _templateWithTimeBlocks.value
        if (currentTemplateWithTimeBlocks != null) {
            val updatedBlocks = currentTemplateWithTimeBlocks.timeBlocks.filterNot { it.id == timeBlock.id }
            _templateWithTimeBlocks.value = currentTemplateWithTimeBlocks.copy(timeBlocks = updatedBlocks)
            _timeBlocks.value = updatedBlocks
        }
    }

    override fun toggleDayAssignment(dayOfWeek: DayOfWeek) {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeBlocksScreenPreview() {
    val dummyTimeBlocks = listOf(
        TimeBlock(1, 1L, "Morning Stroll", LocalTime.of(8, 0), LocalTime.of(9, 0), 1500),
        TimeBlock(2, 1L, "Lunch Break Walk", LocalTime.of(12, 30), LocalTime.of(13, 0), 500),
        TimeBlock(3, 1L, "Evening Power Walk", LocalTime.of(18, 0), LocalTime.of(18, 30), 2000)
    )
    val dummyTemplate = Template(1, "Weekday")
    val dummyTemplateWithTimeBlocks = TemplateWithTimeBlocks(dummyTemplate, dummyTimeBlocks)
    val fakeViewModel = FakeTimeBlocksViewModel(dummyTemplateWithTimeBlocks)
    val showDialogState = remember { mutableStateOf(false) }
    TimeBlocksScreen(
        viewModel = fakeViewModel,
        onNavigateToAdd = {},
        onNavigateToEdit = {},
        contentPadding = PaddingValues(0.dp),
        editableTemplateName = "Weekday Template",
        totalSteps = 4000,
        showEditTemplateNameDialog = showDialogState
    )
}