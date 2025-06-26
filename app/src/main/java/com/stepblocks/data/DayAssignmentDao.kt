package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface DayAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dayAssignment: DayAssignment)

    @Query("SELECT * FROM day_assignments WHERE dayOfWeek = :dayOfWeek")
    fun getTemplateForDay(dayOfWeek: DayOfWeek): Flow<DayAssignment?>
}
