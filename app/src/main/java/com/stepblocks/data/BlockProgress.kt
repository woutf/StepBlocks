package com.stepblocks.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "block_progress",
    primaryKeys = ["date", "timeBlockId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyProgress::class,
            parentColumns = ["date"],
            childColumns = ["date"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TimeBlock::class,
            parentColumns = ["id"],
            childColumns = ["timeBlockId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["timeBlockId"])]
)
data class BlockProgress(
    val date: LocalDate,
    val timeBlockId: Long,
    val actualSteps: Int,
    val targetSteps: Int,
    val startNotificationSent: Boolean,
    val midNotificationSent: Boolean,
    val endNotificationSent: Boolean
)
