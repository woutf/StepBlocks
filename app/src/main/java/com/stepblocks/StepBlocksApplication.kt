
package com.stepblocks

import android.app.Application
import com.stepblocks.data.AppDatabase
import com.stepblocks.repository.OfflineTemplateRepository
import com.stepblocks.repository.TemplateRepository

class StepBlocksApplication : Application() {

    // Using by lazy so the database and repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository: TemplateRepository by lazy { OfflineTemplateRepository(database) }
}
