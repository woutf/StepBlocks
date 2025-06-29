package com.stepblocks.navigation

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.stepblocks.StepBlocksApplication
import com.stepblocks.data.Template
import com.stepblocks.ui.screens.*
import com.stepblocks.viewmodel.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isTopLevel: Boolean = true
) {
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Templates : Screen("templates", "Templates", Icons.Default.List)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.EditCalendar)
    object History : Screen("history", "History", Icons.Default.DateRange)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Child screens
    object AddEditTemplate : Screen("add_edit_template", "Edit Template", Icons.Default.List, false)
    object TimeBlocks : Screen("time_blocks", "Time Blocks", Icons.Default.List, false)
    object AddEditTimeBlock : Screen("add_edit_time_block", "Edit Time Block", Icons.Default.List, false)
}

val topLevelScreens = listOf(
    Screen.Today,
    Screen.Templates,
    Screen.Schedule,
    Screen.History,
    Screen.Settings,
)

val screens = topLevelScreens + listOf(
    Screen.AddEditTemplate,
    Screen.TimeBlocks,
    Screen.AddEditTimeBlock
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as StepBlocksApplication
    val database = application.database

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val currentScreen = screens.find { screen ->
        val route = currentDestination?.route ?: ""
        route.startsWith(screen.route)
    } ?: Screen.Today

    // ViewModel instances for top-level screens or those that need their state lifted
    val addEditTemplateViewModel: AddEditTemplateViewModel = viewModel(
        factory = AddEditTemplateViewModelFactory(application.repository, navBackStackEntry?.savedStateHandle ?: SavedStateHandle())
    )

    val templateViewModel: TemplateViewModel = viewModel(
        factory = TemplateViewModelFactory(application.repository)
    )

    // State for dynamic TopAppBar content
    var topAppBarTitle by remember { mutableStateOf("") }
    var topAppBarActions: @Composable RowScope.() -> Unit by remember { mutableStateOf({}) }
    val showEditTemplateNameDialog = remember { mutableStateOf(false) } // State for TimeBlocksScreen's template name edit dialog

    // Update TopAppBar content based on current route
    LaunchedEffect(currentRoute) {
        val screen = screens.find { currentRoute?.startsWith(it.route) == true }
        topAppBarTitle = screen?.label ?: Screen.Today.label // Default to Today
        topAppBarActions = {} // Reset actions
    }

    // Handle the Add New Template dialog
    var showAddTemplateDialog by remember { mutableStateOf(false) }
    var newTemplateName by remember { mutableStateOf("") }

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
                        templateViewModel.addTemplate(Template(id = 0, name = newTemplateName))
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
                title = { Text(topAppBarTitle) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null && !currentScreen.isTopLevel) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = topAppBarActions // Dynamic actions
            )
        },
        bottomBar = {
            // Hide bottom navigation on child screens
            val bottomNavCurrentScreen = screens.find { screen ->
                val route = currentDestination?.route ?: ""
                route.startsWith(screen.route)
            } ?: Screen.Today

            if (bottomNavCurrentScreen.isTopLevel) {
                NavigationBar {
                    topLevelScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = navController.currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == Screen.Templates.route) {
                FloatingActionButton(onClick = { showAddTemplateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Template")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route
        ) {
            composable(Screen.Today.route) { TodayScreen(contentPadding = innerPadding) }
            composable(Screen.Templates.route) {
                TemplatesScreen(
                    viewModel = templateViewModel,
                    onTemplateClick = { templateId ->
                        navController.navigate("time_blocks/$templateId")
                    },
                    onShowAddTemplateDialog = { showAddTemplateDialog = true },
                    contentPadding = innerPadding
                )
            }
            composable(Screen.Schedule.route) {
                val viewModel: ScheduleViewModel = viewModel(factory = ScheduleViewModelFactory(database.dayAssignmentDao(), database.templateDao()))
                ScheduleScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Screen.History.route) { HistoryScreen(contentPadding = innerPadding) }
            composable(Screen.Settings.route) { SettingsScreen(contentPadding = innerPadding) }

            composable(
                route = "add_edit_template?templateId={templateId}",
                arguments = listOf(navArgument("templateId") { type = NavType.LongType; defaultValue = -1L })
            ) {
                // Update TopAppBar actions for AddEditTemplateScreen
                val isEditing = navBackStackEntry?.arguments?.getLong("templateId") != -1L
                LaunchedEffect(Unit) { // Use Unit as key to run once when this composable enters composition
                    topAppBarTitle = if (isEditing) "Edit Template" else "Add Template"
                    topAppBarActions = {
                        TextButton(onClick = {
                            addEditTemplateViewModel.saveTemplate()
                            navController.popBackStack()
                        }) {
                            Text("Save")
                        }
                    }
                }
                AddEditTemplateScreen(viewModel = addEditTemplateViewModel, contentPadding = innerPadding)
            }

            composable(
                route = "time_blocks/{templateId}",
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: TimeBlocksViewModel = viewModel(factory = TimeBlocksViewModelFactory(application.repository))
                val editableTemplateName by viewModel.editableTemplateName.collectAsState()
                val timeBlocks by viewModel.timeBlocks.collectAsState()
                val totalSteps = remember(timeBlocks) { timeBlocks.sumOf { it.targetSteps } }

                // Update TopAppBar content for TimeBlocksScreen
                LaunchedEffect(editableTemplateName, totalSteps) {
                    topAppBarTitle = editableTemplateName
                    topAppBarActions = {
                        Text(text = "Total: $totalSteps steps")
                        IconButton(onClick = { showEditTemplateNameDialog.value = true }) { // Control dialog via shared state
                            Icon(Icons.Default.Edit, contentDescription = "Edit Template Name")
                        }
                    }
                }

                TimeBlocksScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = {
                        val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
                        navController.navigate("add_edit_time_block/$templateId")
                    },
                    onNavigateToEdit = { timeBlockId ->
                        val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
                        navController.navigate("add_edit_time_block/$templateId?timeBlockId=$timeBlockId")
                    },
                    contentPadding = innerPadding,
                    editableTemplateName = editableTemplateName, // Pass collected state
                    totalSteps = totalSteps, // Pass calculated state
                    showEditTemplateNameDialog = showEditTemplateNameDialog // Pass the mutable state
                )

                // Dialog for editing template name, controlled by AppNavigation's state
                if (showEditTemplateNameDialog.value) {
                    var dialogTemplateName by remember { mutableStateOf(editableTemplateName) }
                    AlertDialog(
                        onDismissRequest = { showEditTemplateNameDialog.value = false },
                        title = { Text("Edit Template Name") },
                        text = {
                            TextField( // Changed to TextField for consistency with Add Template dialog
                                value = dialogTemplateName,
                                onValueChange = { dialogTemplateName = it },
                                label = { Text("Template Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.updateTemplateName(dialogTemplateName)
                                showEditTemplateNameDialog.value = false
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showEditTemplateNameDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
            composable(
                route = "add_edit_time_block/{templateId}?timeBlockId={timeBlockId}",
                arguments = listOf(
                    navArgument("templateId") { type = NavType.LongType },
                    navArgument("timeBlockId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val viewModel: AddEditTimeBlockViewModel = viewModel(
                    factory = AddEditTimeBlockViewModelFactory(application.repository, backStackEntry.savedStateHandle)
                )
                val uiState by viewModel.uiState.collectAsState()

                // Update TopAppBar actions for AddEditTimeBlockScreen
                LaunchedEffect(uiState.isEditing, uiState.name, uiState.targetStepsError, uiState.timeRangeError, uiState.overlapError) {
                    topAppBarTitle = if (uiState.isEditing) "Edit Time Block" else "Add Time Block"
                    topAppBarActions = {
                        val isSaveEnabled = uiState.name.isNotBlank() && uiState.targetStepsError == null && uiState.timeRangeError == null && uiState.overlapError == null
                        TextButton(onClick = {
                            viewModel.saveTimeBlock()
                            // Navigating up happens after isTimeBlockSaved becomes true in LaunchedEffect in AddEditTimeBlockScreen
                        }, enabled = isSaveEnabled) {
                            Text("Done")
                        }
                    }
                }

                AddEditTimeBlockScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding,
                    onSave = { navController.popBackStack() } // Callback to navigate up after save
                )
            }
        }
    }
}
