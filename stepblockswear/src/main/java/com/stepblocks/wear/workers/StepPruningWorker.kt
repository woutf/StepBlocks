package com.stepblocks.wear.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stepblocks.wear.data.StepDatabase
import java.util.concurrent.TimeUnit

class StepPruningWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val stepDao by lazy { StepDatabase.getDatabase(applicationContext).stepDao() }

    override suspend fun doWork(): Result {
        return try {
            val cutoffTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(48)
            stepDao.pruneOldData(cutoffTimestamp)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
