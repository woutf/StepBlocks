
package com.stepblocks.repository

import com.stepblocks.data.AppDatabase
import com.stepblocks.data.Template
import com.stepblocks.data.TimeBlock
import kotlinx.coroutines.flow.Flow

/**
 * The offline implementation of the [TemplateRepository]. This class
 * retrieves and saves data from the local Room database.
 */
class OfflineTemplateRepository(private val db: AppDatabase) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<Template>> = db.templateDao().getAllTemplates()

    override suspend fun getTemplateById(id: Long): Template? = db.templateDao().getTemplateById(id)

    override suspend fun insertTemplate(template: Template) {
        db.templateDao().insertTemplate(template)
    }

    override suspend fun deleteTemplate(template: Template) {
        db.templateDao().deleteTemplate(template)
    }

    override fun getTimeBlocksForTemplate(templateId: Long): Flow<List<TimeBlock>> {
        return db.timeBlockDao().getTimeBlocksForTemplate(templateId)
    }

    override suspend fun getTimeBlockById(id: Long): TimeBlock? = db.timeBlockDao().getTimeBlockById(id)

    override suspend fun insertTimeBlock(timeBlock: TimeBlock) {
        db.timeBlockDao().insertTimeBlock(timeBlock)
    }
}
