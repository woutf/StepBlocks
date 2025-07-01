package com.stepblocks.wear.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stepblocks.wear.data.CachedStepEntity
import com.stepblocks.wear.data.StepDatabase
import com.stepblocks.wear.services.StepTrackingService
import kotlinx.coroutines.flow.first

class StepCacheWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val stepDao by lazy { StepDatabase.getDatabase(applicationContext).stepDao() }

    override suspend fun doWork(): Result {
        return try {
            val unsyncedSteps = StepTrackingService.unsyncedStepCount.first()

            if (unsyncedSteps > 0) {
                val cachedStepEntity = CachedStepEntity(
                    timestamp = System.currentTimeMillis(),
                    stepDelta = unsyncedSteps,
                    synced = false
                )
                stepDao.insert(cachedStepEntity)
                // Reset the counter after caching
                StepTrackingService.unsyncedStepCount.value = 0
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
