package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.viewmodel.ITimeBlocksViewModel
import com.stepblocks.viewmodel.TimeBlocksViewModel
import com.stepblocks.viewmodel.TimeBlocksViewModelFactory
import com.stepblocks.data.TimeBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlocksScreen(
    templateId: Long,
    viewModel: ITimeBlocksViewModel = viewModel<TimeBlocksViewModel>(factory = TimeBlocksViewModelFactory(templateId)),
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val timeBlocks by viewModel.timeBlocks.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var timeBlockToDelete by remember { mutableStateOf<TimeBlock?>(null) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Time Blocks") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Time Block")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
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
        }
    }
}

// Preview using a fake ViewModel that implements the interface
class FakeTimeBlocksViewModel(
    fakeTimeBlocks: List<TimeBlock>
) : ITimeBlocksViewModel {
    private val _timeBlocks = MutableStateFlow(fakeTimeBlocks)
    override val timeBlocks: StateFlow<List<TimeBlock>> = _timeBlocks
    override fun deleteTimeBlock(timeBlock: TimeBlock) {
        _timeBlocks.value = _timeBlocks.value.filterNot { it.id == timeBlock.id }
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
    val fakeViewModel = FakeTimeBlocksViewModel(dummyTimeBlocks)
    TimeBlocksScreen(
        templateId = 1L,
        viewModel = fakeViewModel,
        onNavigateToAdd = {},
        onNavigateToEdit = {}
    )
}
