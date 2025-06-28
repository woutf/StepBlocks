package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.Template
import com.stepblocks.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val templates by viewModel.templates.collectAsState()
    val assignments by viewModel.assignments.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Assign Templates to Days", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        DayOfWeek.values().forEach { day ->
            DayRow(
                day = day,
                templates = templates,
                selectedTemplateId = assignments.find { it.dayOfWeek == day }?.templateId,
                onTemplateSelected = { templateId ->
                    viewModel.onAssignmentChange(day, templateId)
                }
            )
            Divider()
        }
    }
}

@Composable
fun DayRow(
    day: DayOfWeek,
    templates: List<Template>,
    selectedTemplateId: Long?,
    onTemplateSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
        Box {
            Button(onClick = { expanded = true }) {
                Text(templates.find { it.id == selectedTemplateId }?.name ?: "Select Template")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                templates.forEach { template ->
                    DropdownMenuItem(
                        text = { Text(template.name) },
                        onClick = {
                            onTemplateSelected(template.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}