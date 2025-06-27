
package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_blocks WHERE templateId = :templateId ORDER BY startTime")
    fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>>

    @Query("SELECT * FROM time_blocks WHERE id = :id")
    suspend fun getTimeBlockById(id: Long): TimeBlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlock(timeBlock: TimeBlock)

    @Update
    suspend fun updateTimeBlock(timeBlock: TimeBlock)

    @Delete
    suspend fun deleteTimeBlock(timeBlock: TimeBlock)
}
