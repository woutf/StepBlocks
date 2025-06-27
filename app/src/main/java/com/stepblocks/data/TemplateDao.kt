package com.stepblocks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)

    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<Template>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): Template?

    @Transaction
    @Query("SELECT * FROM templates WHERE id = :id")
    fun getTemplateWithTimeBlocks(id: Long): Flow<TemplateWithTimeBlocks>

    @Transaction
    @Query("SELECT * FROM templates")
    fun getAllTemplatesWithTimeBlocks(): Flow<List<TemplateWithTimeBlocks>>
}
