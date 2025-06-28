package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BlockProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockProgress(blockProgress: BlockProgress)

    @Update
    suspend fun updateBlockProgress(blockProgress: BlockProgress)

    @Query("SELECT * FROM block_progress WHERE date = :date")
    fun getBlockProgressForDailyProgress(date: LocalDate): Flow<List<BlockProgress>>
}
