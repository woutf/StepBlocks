package com.stepblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey val date: Date,
    val templateId: String,
    val blockProgress: List<BlockProgress>
)
