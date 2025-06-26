
package com.stepblocks.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stepblocks.StepBlocksApplication
import com.stepblocks.ui.screens.AddEditTemplateScreen
import com.stepblocks.ui.screens.AddEditTimeBlockScreenWithPicker
import com.stepblocks.ui.screens.TemplatesScreen
import com.stepblocks.ui.screens.TimeBlocksScreen
import com.stepblocks.viewmodel.AddEditTemplateViewModelFactory
import com.stepblocks.viewmodel.AddEditTimeBlockViewModelFactory
import com.stepblocks.viewmodel.TemplateViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as StepBlocksApplication

    NavHost(navController = navController, startDestination = "templates") {
        composable("templates") {
            TemplatesScreen(
                viewModel = viewModel(
                    factory = TemplateViewModelFactory(application.repository)
                ),
                onTemplateClick = { templateId ->
                    navController.navigate("time_blocks/$templateId")
                },
                onAddTemplate = {
                    navController.navigate("add_edit_template")
                },
                onEditTemplate = { templateId ->
                    navController.navigate("add_edit_template?templateId=$templateId")
                }
            )
        }
        composable(
            route = "add_edit_template?templateId={templateId}",
            arguments = listOf(navArgument("templateId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            AddEditTemplateScreen(
                navController = navController,
                viewModel = viewModel(
                    factory = AddEditTemplateViewModelFactory(
                        application.repository,
                        backStackEntry.savedStateHandle
                    )
                )
            )
        }
        composable(
            route = "time_blocks/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.LongType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
            TimeBlocksScreen(
                templateId = templateId,
                onNavigateToAdd = {
                    navController.navigate("add_edit_time_block/$templateId")
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
            AddEditTimeBlockScreenWithPicker(
                navController = navController,
                viewModel = viewModel(
                    factory = AddEditTimeBlockViewModelFactory(
                        repository = application.repository,
                        savedStateHandle = backStackEntry.savedStateHandle
                    )
                )
            )
        }
    }
}
