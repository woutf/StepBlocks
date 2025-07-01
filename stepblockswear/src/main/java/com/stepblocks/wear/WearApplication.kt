package com.stepblocks.wear

import android.app.Application
import androidx.work.Configuration
import com.stepblocks.wear.data.AppDatabase
import com.stepblocks.wear.workers.StepCacheWorker
import com.stepblocks.wear.workers.StepPruningWorker
import androidx.work.WorkerFactory
import androidx.work.ListenableWorker
import android.content.Context
import androidx.work.WorkerParameters

class WearApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(AppWorkerFactory(applicationContext))
            .build()
}

class AppWorkerFactory(private val context: Context) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        return when (workerClassName) {
            StepCacheWorker::class.java.name -> StepCacheWorker(appContext, workerParameters)
            StepPruningWorker::class.java.name -> StepPruningWorker(appContext, workerParameters)
            else -> null
        }
    }
}
