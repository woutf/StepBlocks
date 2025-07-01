package com.stepblocks.wear.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters

class StepPruningWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // TODO: Implement logic to prune old step data from Room
        return Result.success()
    }
}
