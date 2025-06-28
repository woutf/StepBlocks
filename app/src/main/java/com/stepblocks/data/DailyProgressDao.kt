package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import java.util.Date

@Dao
interface DailyProgressDao {
    @Insert
    suspend fun insertDailyProgress(dailyProgress: DailyProgress)

    @Update
    suspend fun updateDailyProgress(dailyProgress: DailyProgress)

    @Query("SELECT * FROM daily_progress")
    suspend fun getAll(): List<DailyProgress>

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    suspend fun getDailyProgressByDate(date: Date): DailyProgress?
    
    @Transaction
    @Query("SELECT * FROM templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): TemplateWithTimeBlocks?
}
