package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BlockProgressDao {
    @Insert
    suspend fun insert(blockProgress: BlockProgress)

    @Update
    suspend fun update(blockProgress: BlockProgress)

    @Query("SELECT * FROM block_progress")
    suspend fun getAll(): List<BlockProgress>
}
