
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
import com.stepblocks.data.Template
import com.stepblocks.data.TimeBlock
import com.stepblocks.repository.TemplateRepository
import com.stepblocks.ui.theme.StepBlocksTheme
import com.stepblocks.viewmodel.TemplateViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: TemplateViewModel,
    onTemplateClick: (Long) -> Unit,
    onAddTemplate: () -> Unit,
    onEditTemplate: (Long) -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<Template?>(null) }

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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Templates") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTemplate) {
                Icon(Icons.Filled.Add, contentDescription = "Add Template")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(
                items = templates,
                key = { it.id }
            ) { template ->
                TemplateCard(
                    template = template,
                    onCardClick = { onTemplateClick(template.id) },
                    onEdit = { onEditTemplate(template.id) },
                    onDelete = {
                        templateToDelete = template
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
            TimeBlock(1, 1, "Block 1", LocalTime.now(), LocalTime.now(), 100, false, false, false)
        )
    )


    override fun getAllTemplates(): Flow<List<Template>> = fakeTemplates

    override suspend fun getTemplateById(id: Long): Template? =
        fakeTemplates.value.find { it.id == id }

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
}

@Preview(showBackground = true)
@Composable
fun TemplatesScreenPreview() {
    StepBlocksTheme {
        val fakeRepository = FakeTemplateRepository()
        val mockViewModel = TemplateViewModel(fakeRepository)
        TemplatesScreen(mockViewModel, {}, {}, {})
    }
}
