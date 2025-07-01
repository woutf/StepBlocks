package com.stepblocks.wear.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_step_deltas")
data class CachedStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long, // End time of the interval
    val stepDelta: Int,  // Incremental steps for this 15-minute interval
    val synced: Boolean = false
)
