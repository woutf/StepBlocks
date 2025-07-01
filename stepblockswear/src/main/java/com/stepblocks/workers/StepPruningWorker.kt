package com.stepblocks.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class StepPruningWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Step data pruning logic will go here
        return Result.success()
    }

    companion object {
        fun enqueue(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<StepPruningWorker>(24, TimeUnit.HOURS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "step-pruning-worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
