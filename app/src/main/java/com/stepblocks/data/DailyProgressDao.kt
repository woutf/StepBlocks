package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface DailyProgressDao {
    @Insert
    suspend fun insert(dailyProgress: DailyProgress)

    @Update
    suspend fun update(dailyProgress: DailyProgress)

    @Query("SELECT * FROM daily_progress")
    suspend fun getAll(): List<DailyProgress>

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    suspend fun getByDate(date: Date): DailyProgress?
}
