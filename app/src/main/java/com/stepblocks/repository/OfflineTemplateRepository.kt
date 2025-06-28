package com.stepblocks.repository

import com.stepblocks.data.AppDatabase
import com.stepblocks.data.DayAssignment
import com.stepblocks.data.DayOfWeek
import com.stepblocks.data.Template
import com.stepblocks.data.TemplateWithTimeBlocks
import com.stepblocks.data.TimeBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The offline implementation of the [TemplateRepository]. This class
 * retrieves and saves data from the local Room database.
 */
class OfflineTemplateRepository(private val db: AppDatabase) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<Template>> = db.templateDao().getAllTemplates()

    override fun getAllTemplatesWithTimeBlocks(): Flow<List<TemplateWithTimeBlocks>> = db.templateDao().getAllTemplatesWithTimeBlocks()

    override fun getTemplateWithTimeBlocks(id: Long): Flow<TemplateWithTimeBlocks?> {
        return db.templateDao().getTemplateWithTimeBlocks(id)
            .distinctUntilChanged()
            .conflate()
    }

    override fun getTemplateFlow(id: Long): Flow<Template?> = db.templateDao().getTemplateFlow(id)

    override suspend fun getTemplateById(id: Long): Template? = db.templateDao().getTemplateById(id)

    override suspend fun insertTemplate(template: Template) {
        db.templateDao().insertTemplate(template)
    }

    override suspend fun updateTemplate(template: Template) {
        db.templateDao().updateTemplate(template)
    }

    override suspend fun deleteTemplate(template: Template) {
        db.templateDao().deleteTemplate(template)
    }

    override fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>> {
        return db.templateDao().getTimeBlocksForTemplate(templateId)
            .distinctUntilChanged()
            .conflate()
    }

    override suspend fun getTimeBlockById(id: Long): TimeBlock? = db.templateDao().getTimeBlockById(id)

    override suspend fun insertTimeBlock(timeBlock: TimeBlock) {
        db.templateDao().insertTimeBlock(timeBlock)
    }

    override suspend fun updateTimeBlock(timeBlock: TimeBlock) {
        db.templateDao().updateTimeBlock(timeBlock)
    }

    override suspend fun deleteTimeBlock(timeBlock: TimeBlock) {
        db.templateDao().deleteTimeBlock(timeBlock)
    }

    override fun getDayAssignmentsForTemplate(templateId: Long): Flow<List<DayAssignment>> = db.dayAssignmentDao().getDayAssignmentsForTemplate(templateId)

    override suspend fun insertDayAssignment(dayAssignment: DayAssignment) = db.dayAssignmentDao().insertDayAssignment(dayAssignment)

    override suspend fun deleteDayAssignment(templateId: Long, dayOfWeek: DayOfWeek) = db.dayAssignmentDao().deleteDayAssignment(templateId, dayOfWeek)
}