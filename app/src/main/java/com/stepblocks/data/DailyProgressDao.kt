package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface DailyProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyProgress(dailyProgress: DailyProgress)

    @Update
    suspend fun updateDailyProgress(dailyProgress: DailyProgress)

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    suspend fun getDailyProgressByDate(date: Date): DailyProgress?
}
