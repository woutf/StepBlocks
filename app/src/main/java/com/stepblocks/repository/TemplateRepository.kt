
package com.stepblocks.repository

import com.stepblocks.data.Template
import com.stepblocks.data.TimeBlock
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the repository that handles Template data.
 */
interface TemplateRepository {
    fun getAllTemplates(): Flow<List<Template>>
    suspend fun getTemplateById(id: Long): Template?
    suspend fun insertTemplate(template: Template)
    suspend fun deleteTemplate(template: Template)
    fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>>
    suspend fun getTimeBlockById(id: Long): TimeBlock?
    suspend fun insertTimeBlock(timeBlock: TimeBlock)
}
