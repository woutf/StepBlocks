package com.stepblocks.data

import androidx.room.Embedded
import androidx.room.Relation

data class DailyProgressWithBlockProgress(
    @Embedded val dailyProgress: DailyProgress,
    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    val blockProgress: List<BlockProgress>
)
