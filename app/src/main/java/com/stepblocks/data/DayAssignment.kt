package com.stepblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_assignments", primaryKeys = ["templateId", "dayOfWeek"])
data class DayAssignment(
    val templateId: Long,
    val dayOfWeek: DayOfWeek
)
