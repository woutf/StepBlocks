
package com.stepblocks.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
@Entity(
    tableName = "time_blocks",
    foreignKeys = [
        ForeignKey(
            entity = Template::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["templateId"])]
)
data class TimeBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val name: String,
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime,
    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime,
    val targetSteps: Int,
    val notifyStart: Boolean = false,
    val notifyMid: Boolean = false,
    val notifyEnd: Boolean = false
)
