package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.stepblocks.data.DayAssignment
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.data.TimeBlock
import com.stepblocks.repository.TemplateRepository
import com.stepblocks.ui.theme.StepBlocksTheme
import com.stepblocks.viewmodel.TemplateViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import androidx.compose.material3.TopAppBarDefaults // Import TopAppBarDefaults
import androidx.compose.material3.MaterialTheme // Add this import
import kotlinx.coroutines.flow.map
import androidx.compose.material.icons.filled.Settings // Import Settings icon
import androidx.compose.material3.IconButton // Import IconButton
import androidx.compose.material3.TextField // Import TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: TemplateViewModel,
    onTemplateClick: (Long) -> Unit,
    onEditTemplate: (Long) -> Unit,
) {
    val templatesWithTimeBlocks by viewModel.templates.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<Template?>(null) }
    var showAddTemplateDialog by remember { mutableStateOf(false) } // State for dialog visibility
    var newTemplateName by remember { mutableStateOf("") } // State for TextField input

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                templateToDelete = null
            },
            title = { Text("Delete Template") },
            text = { Text("Are you sure you want to delete '${templateToDelete?.name}'?") },
            confirmButton = {
                Button(onClick = {
                    templateToDelete?.let { viewModel.deleteTemplate(it) }
                    showDeleteConfirmation = false
                    templateToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmation = false
                    templateToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddTemplateDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTemplateDialog = false
                newTemplateName = ""
            },
            title = { Text("Add New Template") },
            text = {
                TextField(
                    value = newTemplateName,
                    onValueChange = { newTemplateName = it },
                    label = { Text("Template Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newTemplateName.isNotBlank()) {
                        viewModel.addTemplate(Template(id = 0, name = newTemplateName))
                        showAddTemplateDialog = false
                        newTemplateName = ""
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAddTemplateDialog = false
                    newTemplateName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTemplateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Template")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(
                items = templatesWithTimeBlocks,
                key = { it.template.id }
            ) { templateWithTimeBlocks ->
                val totalSteps = templateWithTimeBlocks.timeBlocks.sumOf { it.targetSteps }
                TemplateCard(
                    template = templateWithTimeBlocks.template,
                    totalSteps = totalSteps,
                    onCardClick = { onTemplateClick(templateWithTimeBlocks.template.id) },
                    onEdit = { onTemplateClick(templateWithTimeBlocks.template.id) }, // CHANGED THIS
                    onDelete = {
                        templateToDelete = templateWithTimeBlocks.template
                        showDeleteConfirmation = true
                    }
                )
            }
        }
    }
}

// --- Preview ---

private class FakeTemplateRepository : TemplateRepository {
    private val fakeTemplates = MutableStateFlow(
        listOf(
            Template(id = 1, name = "Morning Routine"),
            Template(id = 2, name = "Workout")
        )
    )
    private val fakeTimeBlocks = MutableStateFlow(
        listOf(
            TimeBlock(1, 1, "Block 1", LocalTime.now(), LocalTime.now(), 100)
        )
)

    override fun getAllTemplates(): Flow<List<Template>> = fakeTemplates

    override fun getAllTemplatesWithTimeBlocks(): Flow<List<TemplateWithTimeBlocks>> {
        return flowOf(
            fakeTemplates.value.map { template ->
                TemplateWithTimeBlocks(
                    template = template,
                    timeBlocks = fakeTimeBlocks.value.filter { it.templateId == template.id }
                )
            }
        )
    }

    override fun getTemplateFlow(id: Long): Flow<Template?> {
        return fakeTemplates.map { templates -> templates.find { it.id == id } }
    }

    override suspend fun getTemplateById(id: Long): Template? =
        fakeTemplates.value.find { it.id == id }

    override fun getTemplateWithTimeBlocks(id: Long): Flow<TemplateWithTimeBlocks?> {
        val template = fakeTemplates.value.find { it.id == id }
        val timeBlocks = fakeTimeBlocks.value.filter { it.templateId == id }
        return flowOf(TemplateWithTimeBlocks(template!!, timeBlocks))
    }

    override suspend fun insertTemplate(template: Template) {
        fakeTemplates.update {
            val existing = it.find { t -> t.id == template.id }
            if (existing != null) {
                it.map { t -> if (t.id == template.id) template else t }
            } else {
                it + template.copy(id = (it.maxOfOrNull { t -> t.id } ?: 0) + 1)
            }
        }
    }

    override suspend fun updateTemplate(template: Template) {
        fakeTemplates.update { templates ->
            templates.map { if (it.id == template.id) template else it }
        }
    }

    override suspend fun deleteTemplate(template: Template) {
        fakeTemplates.update {
            it.filterNot { t -> t.id == template.id }
        }
    }

    override fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>> {
        return flowOf(fakeTimeBlocks.value.filter { it.templateId == templateId })
    }

    override suspend fun getTimeBlockById(id: Long): TimeBlock? {
        return fakeTimeBlocks.value.find { it.id == id }
    }

    override suspend fun insertTimeBlock(timeBlock: TimeBlock) {
        fakeTimeBlocks.update { it + timeBlock }
    }

    override suspend fun updateTimeBlock(timeBlock: TimeBlock) {
        fakeTimeBlocks.update { blocks ->
            blocks.map { if (it.id == timeBlock.id) timeBlock else it }
        }
    }

    override suspend fun deleteTimeBlock(timeBlock: TimeBlock) {
        fakeTimeBlocks.update {
            it.filterNot { t -> t.id == timeBlock.id }
        }
    }

    override fun getDayAssignmentsForTemplate(templateId: Long): Flow<List<DayAssignment>> {
        // Dummy implementation for preview
        return flowOf(emptyList())
    }

    override suspend fun insertDayAssignment(dayAssignment: DayAssignment) {
        // Dummy implementation for preview
    }

    override suspend fun deleteDayAssignment(templateId: Long, dayOfWeek: DayOfWeek) {
        // Dummy implementation for preview
    }
}

@Preview(showBackground = true)
@Composable
fun TemplatesScreenPreview() {
    StepBlocksTheme {
        val fakeRepository = FakeTemplateRepository()
        val mockViewModel = TemplateViewModel(fakeRepository)
        TemplatesScreen(mockViewModel, {}, {}) // Removed onAddTemplate from here
    }
}
