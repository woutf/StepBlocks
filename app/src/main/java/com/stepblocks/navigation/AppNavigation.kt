
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
import com.stepblocks.ui.screens.AddEditTimeBlockScreen
import com.stepblocks.ui.screens.TemplatesScreen
import com.stepblocks.ui.screens.TimeBlocksScreen
import com.stepblocks.viewmodel.AddEditTemplateViewModel
import com.stepblocks.viewmodel.AddEditTemplateViewModelFactory
import com.stepblocks.viewmodel.AddEditTimeBlockViewModel
import com.stepblocks.viewmodel.AddEditTimeBlockViewModelFactory
import com.stepblocks.viewmodel.TemplateViewModel
import com.stepblocks.viewmodel.TemplateViewModelFactory
import com.stepblocks.viewmodel.TimeBlocksViewModel
import com.stepblocks.viewmodel.TimeBlocksViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as StepBlocksApplication

    NavHost(navController = navController, startDestination = "templates") {
        composable("templates") {
            val viewModel: TemplateViewModel = viewModel(
                factory = TemplateViewModelFactory(application.repository)
            )
            TemplatesScreen(
                viewModel = viewModel,
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
    }
}
