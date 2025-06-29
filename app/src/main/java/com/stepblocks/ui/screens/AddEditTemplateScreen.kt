package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stepblocks.viewmodel.AddEditTemplateViewModel

@Composable
fun AddEditTemplateScreen(
    viewModel: AddEditTemplateViewModel,
    contentPadding: PaddingValues
) {
    val templateName = viewModel.templateName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = templateName,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Template Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Time Blocks",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
