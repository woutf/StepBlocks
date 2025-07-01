package com.stepblocks.wear.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StepDao {
    @Insert
    suspend fun insert(delta: CachedStepEntity)

    @Query("SELECT * FROM cached_step_deltas WHERE synced = :synced ORDER BY timestamp ASC")
    suspend fun getUnsyncedData(synced: Boolean = false): List<CachedStepEntity>

    @Query("UPDATE cached_step_deltas SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    @Query("DELETE FROM cached_step_deltas WHERE timestamp < :cutoffTimestamp")
    suspend fun pruneOldData(cutoffTimestamp: Long)
}
