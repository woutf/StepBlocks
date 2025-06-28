package com.stepblocks.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.stepblocks.ui.screens.*
import com.stepblocks.viewmodel.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Templates : Screen("templates", "Templates", Icons.Default.List)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.EditCalendar)
    object History : Screen("history", "History", Icons.Default.DateRange)
}

val items = listOf(
    Screen.Today,
    Screen.Templates,
    Screen.Schedule,
    Screen.History,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as StepBlocksApplication
    val database = application.database

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
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Today.route, Modifier.padding(innerPadding)) {
            composable(Screen.Today.route) {
                val viewModel: TodayViewModel = viewModel(
                    factory = TodayViewModel.provideFactory(
                        application,
                        database.dayAssignmentDao(),
                        database.dailyProgressDao(),
                        database.blockProgressDao(),
                        application.healthConnectManager
                    )
                )
                TodayScreen(viewModel = viewModel)
            }
            composable(Screen.Templates.route) {
                val viewModel: TemplateViewModel = viewModel(
                    factory = TemplateViewModelFactory(application.repository)
                )
                TemplatesScreen(
                    viewModel = viewModel,
                    onTemplateClick = { templateId ->
                        navController.navigate("time_blocks/$templateId")
                    },
                    onEditTemplate = { templateId ->
                        navController.navigate("add_edit_template?templateId=$templateId")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
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
                // History screen composable goes here
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