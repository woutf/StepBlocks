package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update 
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template): Long

    @Update 
    suspend fun updateTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)

    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<Template>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): Template?

    @Query("SELECT * FROM templates WHERE id = :id")
    fun getTemplateFlow(id: Long): Flow<Template?>

    @Transaction
    @Query("SELECT * FROM templates WHERE id = :id")
    fun getTemplateWithTimeBlocks(id: Long): Flow<TemplateWithTimeBlocks?>

    @Transaction
    @Query("SELECT * FROM templates")
    fun getAllTemplatesWithTimeBlocks(): Flow<List<TemplateWithTimeBlocks>>

    @Transaction
    @Query("SELECT * FROM time_blocks WHERE templateId = :templateId ORDER BY startTime ASC")
    fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlock(timeBlock: TimeBlock)

    @Update
    suspend fun updateTimeBlock(timeBlock: TimeBlock)

    @Delete
    suspend fun deleteTimeBlock(timeBlock: TimeBlock)

    @Query("SELECT * FROM time_blocks WHERE id = :id")
    suspend fun getTimeBlockById(id: Long): TimeBlock?

}
