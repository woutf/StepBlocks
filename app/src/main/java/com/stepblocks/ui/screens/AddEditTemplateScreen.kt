package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.stepblocks.viewmodel.AddEditTemplateViewModel
import androidx.compose.material3.TopAppBarDefaults // Import TopAppBarDefaults
import androidx.compose.material3.TextButton // ADD THIS IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTemplateScreen(
    navController: NavController,
    viewModel: AddEditTemplateViewModel
) {
    val templateName = viewModel.templateName

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.templateId == -1L) "New Template" else "Edit Template") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { // CHANGE HERE: IconButton to TextButton
                        viewModel.saveTemplate()
                        navController.popBackStack()
                    }) {
                        Text("Save") // CHANGE HERE: Icon to Text
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding for consistency
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
                modifier = Modifier.padding(top = 16.dp) // Keep current padding for now, might adjust later when time blocks are added
            )
        }
    }
}
