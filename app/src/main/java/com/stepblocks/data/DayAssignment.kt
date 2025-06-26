package com.stepblocks.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "day_assignments",
    primaryKeys = ["dayOfWeek"],
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
data class DayAssignment(
    val dayOfWeek: Int, // 0=Sunday, 6=Saturday
    val templateId: Long
)
