package com.stepblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek as JavaDayOfWeek

@Entity(tableName = "day_assignments")
data class DayAssignment(
    @PrimaryKey val dayOfWeek: DayOfWeek,
    val templateId: Long
)

fun JavaDayOfWeek.toDayOfWeek(): DayOfWeek {
    return when (this) {
        JavaDayOfWeek.MONDAY -> DayOfWeek.MONDAY
        JavaDayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        JavaDayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        JavaDayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        JavaDayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        JavaDayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        JavaDayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
    }
}
