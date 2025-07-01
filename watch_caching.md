Task: Watch Data Caching & Pruning
Objective: Implement a robust local caching and data management strategy on the watch to ensure no steps are lost during periods of disconnection.

Functional Requirements
1. Implement Step Caching Worker
   Requirement: FR-C.1

File to Modify: stepblockswear/src/main/java/com/stepblocks/wear/workers/StepCacheWorker.kt

Action:

Implement the doWork() method.

The worker should retrieve the current unsyncedStepCount from the StepTrackingService.

It will then create a new CachedStepEntity object with the current timestamp and the step count.

The synced property of the new entity must be set to false.

Insert this new entity into the local Room database using the StepDao.

Schedule this worker to run periodically (e.g., every 15 minutes) using WorkManager.

2. Implement Data Pruning Worker
   Requirement: FR-C.2

File to Modify: stepblockswear/src/main/java/com/stepblocks/wear/workers/StepPruningWorker.kt

Action:

Implement the doWork() method.

Calculate a cutoff timestamp by subtracting 48 hours from the current time.

Call the pruneOldData(cutoffTimestamp) function in the StepDao to delete all records from the cached_step_deltas table that are older than the cutoff.

Schedule this worker to run periodically (e.g., once every 24 hours) using WorkManager.