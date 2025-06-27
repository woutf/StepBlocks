
package com.stepblocks.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "time_blocks",
    foreignKeys = [
        ForeignKey(
            entity = Template::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimeBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val targetSteps: Int,
    val notifyStart: Boolean,
    val notifyMid: Boolean,
    val notifyEnd: Boolean
)
