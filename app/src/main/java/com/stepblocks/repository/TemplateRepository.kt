
package com.stepblocks.repository

import com.stepblocks.data.DayAssignment
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.data.TimeBlock
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the repository that handles Template data.
 */
interface TemplateRepository {
    fun getAllTemplates(): Flow<List<Template>>
    fun getAllTemplatesWithTimeBlocks(): Flow<List<TemplateWithTimeBlocks>>
    fun getTemplateWithTimeBlocks(id: Long): Flow<TemplateWithTimeBlocks?>
    fun getTemplateFlow(id: Long): Flow<Template?>
    suspend fun getTemplateById(id: Long): Template?
    suspend fun insertTemplate(template: Template)
    suspend fun updateTemplate(template: Template)
    suspend fun deleteTemplate(template: Template)
    fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>>
    suspend fun getTimeBlockById(id: Long): TimeBlock?
    suspend fun insertTimeBlock(timeBlock: TimeBlock)
    suspend fun updateTimeBlock(timeBlock: TimeBlock)
    suspend fun deleteTimeBlock(timeBlock: TimeBlock)

    // DayAssignment functions
    fun getDayAssignmentsForTemplate(templateId: Long): Flow<List<DayAssignment>>
    suspend fun insertDayAssignment(dayAssignment: DayAssignment)
    suspend fun deleteDayAssignment(templateId: Long, dayOfWeek: DayOfWeek)
}
