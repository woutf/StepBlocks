package com.stepblocks.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class StepCacheWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Step caching logic will go here
        return Result.success()
    }

    companion object {
        fun enqueue(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<StepCacheWorker>(15, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "step-cache-worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
