package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepblocks.data.BlockProgress
import com.stepblocks.ui.theme.StepBlocksTheme

@Composable
fun TodayScreen(
    viewModel: TodayViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = uiState.templateName,
                    style = MaterialTheme.typography.headlineMedium
                )

                // Daily Progress
                ProgressCircle(
                    progress = (uiState.dailyActualSteps.toFloat() / uiState.dailyTargetSteps.toFloat()).coerceIn(0f, 1f),
                    actualSteps = uiState.dailyActualSteps,
                    targetSteps = uiState.dailyTargetSteps,
                    label = "Today"
                )

                // Active Block Progress
                uiState.activeBlockName?.let {
                    ProgressCircle(
                        progress = (uiState.activeBlockActualSteps.toFloat() / uiState.activeBlockTargetSteps.toFloat()).coerceIn(0f, 1f),
                        actualSteps = uiState.activeBlockActualSteps,
                        targetSteps = uiState.activeBlockTargetSteps,
                        label = it
                    )
                }

                // All Blocks List
                uiState.blockProgress.forEach { block ->
                    BlockProgressRow(block = block)
                }
            }
        }
    }
}

@Composable
fun ProgressCircle(
    progress: Float,
    actualSteps: Int,
    targetSteps: Int,
    label: String
) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(200.dp),
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(text = "$actualSteps", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp))
            Text(text = "/ $targetSteps", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun BlockProgressRow(block: BlockProgress) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = block.blockName, style = MaterialTheme.typography.bodyLarge)
        Text(text = "${block.actualSteps} / ${block.targetSteps}", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun TodayScreenPreview() {
    StepBlocksTheme {
        // You can create a fake ViewModel or pass a preview-specific UiState here
        // For simplicity, we'll just show the empty state for now.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Weekday",
                    style = MaterialTheme.typography.headlineMedium
                )
                ProgressCircle(progress = 0.75f, actualSteps = 7500, targetSteps = 10000, label = "Today")
                ProgressCircle(progress = 0.5f, actualSteps = 1000, targetSteps = 2000, label = "Morning")
                BlockProgressRow(block = BlockProgress(blockName = "Morning", actualSteps = 1000, targetSteps = 2000, startNotificationSent = false, midNotificationSent = false, endNotificationSent = false))
                BlockProgressRow(block = BlockProgress(blockName = "Afternoon", actualSteps = 500, targetSteps = 2000, startNotificationSent = false, midNotificationSent = false, endNotificationSent = false))
            }
        }
    }
}
