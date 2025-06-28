package com.stepblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_progress")
data class BlockProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockName: String,
    val actualSteps: Int,
    val targetSteps: Int,
    val startNotificationSent: Boolean,
    val midNotificationSent: Boolean,
    val endNotificationSent: Boolean
)
