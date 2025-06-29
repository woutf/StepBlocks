package com.stepblocks.navigation

import android.app.Application // Added import
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stepblocks.StepBlocksApplication
import com.stepblocks.data.Template
import com.stepblocks.ui.screens.*
import com.stepblocks.viewmodel.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Templates : Screen("templates", "Templates", Icons.Default.List)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.EditCalendar)
    object History : Screen("history", "History", Icons.Default.DateRange)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val items = listOf(
    Screen.Today,
    Screen.Templates,
    Screen.Schedule,
    Screen.History,
    Screen.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as StepBlocksApplication
    val database = application.database

    // ViewModel for the Templates screen to handle dialog logic
    val templateViewModel: TemplateViewModel = viewModel(
        factory = TemplateViewModelFactory(application.repository)
    )

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
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (currentDestination?.route == Screen.Templates.route) {
                FloatingActionButton(onClick = { showAddTemplateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Template")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                val viewModel: TodayViewModel = viewModel(
                    factory = TodayViewModelFactory(application.applicationContext as Application) // Corrected factory usage
                )
                TodayScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
            composable(Screen.Templates.route) {
                TemplatesScreen(
                    viewModel = templateViewModel, // Re-use the hoisted ViewModel
                    onTemplateClick = { templateId ->
                        navController.navigate("time_blocks/$templateId")
                    },
                    onEditTemplate = { templateId ->
                        navController.navigate("add_edit_template?templateId=$templateId")
                    },
                    onShowAddTemplateDialog = { showAddTemplateDialog = true }
                )
            }

            composable(Screen.Schedule.route) {
                val viewModel: ScheduleViewModel = viewModel(
                    factory = ScheduleViewModelFactory(
                        database.dayAssignmentDao(),
                        database.templateDao()
                    )
                )
                ScheduleScreen(viewModel = viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(
                route = "add_edit_template?templateId={templateId}",
                arguments = listOf(navArgument("templateId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val viewModel: AddEditTemplateViewModel = viewModel(
                    factory = AddEditTemplateViewModelFactory(
                        application.repository,
                        backStackEntry.savedStateHandle
                    )
                )
                AddEditTemplateScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(
                route = "time_blocks/{templateId}",
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: TimeBlocksViewModel = viewModel(
                    factory = TimeBlocksViewModelFactory(application.repository)
                )
                TimeBlocksScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = {
                        val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
                        navController.navigate("add_edit_time_block/$templateId")
                    },
                    onNavigateToEdit = { timeBlockId ->
                        val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
                        navController.navigate("add_edit_time_block/$templateId?timeBlockId=$timeBlockId")
                    }
                )
            }
            composable(
                route = "add_edit_time_block/{templateId}?timeBlockId={timeBlockId}",
                arguments = listOf(
                    navArgument("templateId") { type = NavType.LongType },
                    navArgument("timeBlockId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val viewModel: AddEditTimeBlockViewModel = viewModel(
                    factory = AddEditTimeBlockViewModelFactory(
                        application.repository,
                        backStackEntry.savedStateHandle
                    )
                )
                AddEditTimeBlockScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}