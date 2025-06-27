package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM DayAssignment WHERE templateId = :templateId")
    fun getDayAssignmentsForTemplate(templateId: Long): Flow<List<DayAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayAssignment(dayAssignment: DayAssignment)

    @Query("DELETE FROM DayAssignment WHERE templateId = :templateId AND dayOfWeek = :dayOfWeek")
    suspend fun deleteDayAssignment(templateId: Long, dayOfWeek: Int)
}
