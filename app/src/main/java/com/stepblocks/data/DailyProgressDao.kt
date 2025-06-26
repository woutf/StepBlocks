package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailyProgress: DailyProgress)

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    fun getProgressForDate(date: LocalDate): Flow<DailyProgress?>
}
