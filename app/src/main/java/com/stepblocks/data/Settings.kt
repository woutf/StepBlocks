package com.stepblocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stepblocks.viewmodel.VibrationPattern

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val progressUpdatesEnabled: Boolean = false,
    val beginBlockUpdates: Boolean = false,
    val midBlockUpdates: Boolean = false,
    val endBlockUpdates: Boolean = false,
    val behindTargetPattern: VibrationPattern = VibrationPattern.NONE,
    val onTargetPattern: VibrationPattern = VibrationPattern.NONE,
    val aheadTargetPattern: VibrationPattern = VibrationPattern.NONE
)
