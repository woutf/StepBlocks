package com.stepblocks.wear.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters

class StepCacheWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // TODO: Implement logic to save current unsynced step count to Room
        return Result.success()
    }
}
