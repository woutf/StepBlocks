package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.stepblocks.data.Template
import com.stepblocks.ui.screens.TemplateCard
import com.stepblocks.viewmodel.TemplateViewModel

@Composable
fun TemplatesScreen(
    viewModel: TemplateViewModel,
    onTemplateClick: (Long) -> Unit,
    onShowAddTemplateDialog: () -> Unit,
    contentPadding: PaddingValues
) {
    val templatesWithTimeBlocks by viewModel.templates.collectAsState()
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

    Box(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = templatesWithTimeBlocks,
                key = { it.template.id }
            ) { templateWithTimeBlocks ->
                val totalSteps = templateWithTimeBlocks.timeBlocks.sumOf { it.targetSteps }
                TemplateCard(
                    template = templateWithTimeBlocks.template,
                    totalSteps = totalSteps,
                    onCardClick = { onTemplateClick(templateWithTimeBlocks.template.id) },
                    onEdit = { onTemplateClick(templateWithTimeBlocks.template.id) },
                    onDelete = {
                        templateToDelete = templateWithTimeBlocks.template
                        showDeleteConfirmation = true
                    }
                )
            }
        }
    }
}
