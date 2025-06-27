package com.stepblocks.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stepblocks.data.TimeBlock
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.material3.CardDefaults // ADD THIS IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlockCard(
    timeBlock: TimeBlock,
    onEdit: (Long) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onEdit(timeBlock.id) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp) // CORRECTED LINE
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeBlock.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${timeBlock.startTime.format(formatter)} - ${timeBlock.endTime.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${timeBlock.targetSteps} steps",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Time Block")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeBlockCardPreview() {
    TimeBlockCard(
        timeBlock = TimeBlock(
            id = 1,
            templateId = 1,
            name = "Morning Walk",
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(9, 0),
            targetSteps = 2000,
            notifyStart = true,
            notifyMid = true,
            notifyEnd = false
        ),
        onEdit = {},
        onDelete = {}
    )
}
