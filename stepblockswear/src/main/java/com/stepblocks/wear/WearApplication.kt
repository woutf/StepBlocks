package com.stepblocks.wear

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.stepblocks.wear.workers.StepCacheWorker
import com.stepblocks.wear.workers.StepPruningWorker
import java.util.concurrent.TimeUnit
import androidx.work.WorkerFactory
import androidx.work.ListenableWorker
import android.content.Context
import androidx.work.WorkerParameters

class WearApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(AppWorkerFactory(applicationContext))
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)

        val cacheRequest = PeriodicWorkRequestBuilder<StepCacheWorker>(15, TimeUnit.MINUTES)
            .build()

        val pruningRequest = PeriodicWorkRequestBuilder<StepPruningWorker>(24, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "step-cache-worker",
            ExistingPeriodicWorkPolicy.KEEP,
            cacheRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "step-pruning-worker",
            ExistingPeriodicWorkPolicy.KEEP,
            pruningRequest
        )
    }
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
