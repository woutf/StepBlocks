package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update // Import Update
import androidx.room.Transaction // Import Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyProgress(dailyProgress: DailyProgress) // Renamed from 'insert'

    @Update
    suspend fun updateDailyProgress(dailyProgress: DailyProgress) // Added update function

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    fun getDailyProgressByDate(date: LocalDate): Flow<DailyProgress?> // Renamed from 'getProgressForDate'

    @Transaction
    @Query("SELECT * FROM daily_progress WHERE date = :date")
    fun getDailyProgressWithBlockProgress(date: LocalDate): Flow<DailyProgressWithBlockProgress?> // Added relation query
}
