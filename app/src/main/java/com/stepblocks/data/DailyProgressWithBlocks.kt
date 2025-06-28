package com.stepblocks.data

import androidx.room.Embedded
import androidx.room.Relation

data class DailyProgressWithBlocks(
    @Embedded val dailyProgress: DailyProgress,
    @Relation(
        parentColumn = "id",
        entityColumn = "dailyProgressId"
    ) val blockProgresses: List<BlockProgress>
)
