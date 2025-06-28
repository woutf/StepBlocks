package com.stepblocks.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM day_assignments WHERE templateId = :templateId")
    fun getDayAssignmentsForTemplate(templateId: Long): Flow<List<DayAssignment>>

    @Query("SELECT * FROM day_assignments WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getDayAssignmentForDay(dayOfWeek: DayOfWeek): DayAssignment?

    @Upsert
    suspend fun upsert(dayAssignment: DayAssignment)

    @Query("SELECT * FROM day_assignments")
    fun getAllAssignments(): Flow<List<DayAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayAssignment(dayAssignment: DayAssignment)

    @Query("DELETE FROM day_assignments WHERE templateId = :templateId AND dayOfWeek = :dayOfWeek")
    suspend fun deleteDayAssignment(templateId: Long, dayOfWeek: DayOfWeek)
}
