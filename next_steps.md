# StepBlocks: Real-Time Watch Integration - Implementation Plan

## 1. Overview

This document outlines the high-level technical implementation plan for creating a Wear OS companion app for StepBlocks. The target platform is **Wear OS 3 and higher**.

The primary goal is to provide users with real-time step data from their watch, bypassing the inherent latency of Health Connect's sync cycle. This will be achieved by creating a direct communication channel between the watch and the phone app while maintaining robust data integrity and compatibility within the Health Connect ecosystem.

---

## 2. Core Architecture

The new architecture consists of three main components communicating to provide a seamless experience:

* **Companion Watch App (Wear OS):** The primary source of truth for real-time steps. Operating as a background service with an optional Tile, it collects data directly from on-device sensors via Health Services, caches it locally, and transmits it to the phone app.
* **Phone App (StepBlocks):** The central hub. It listens for updates from the watch, updates its UI in real-time, and is responsible for writing the authoritative step data to Health Connect with the correct metadata to ensure priority and prevent duplication.
* **Wear OS Data Layer:** The communication bridge between the watch and phone app. It will be used for both high-frequency real-time updates and larger, batched data recovery payloads.

---

## 3. Phase 1: Wear OS Companion App Development

### 3.1. Project Setup & Dependencies

* Create a new Wear OS module within the existing Android Studio project, targeting Wear OS 3+ (minSDK 30).
* Add the following dependencies to the `build.gradle.kts` file for the Wear OS module:
    ```kotlin
    // Health Services for sensor data
    implementation("androidx.health:health-services-client:1.1.0-alpha07")

    // Wear OS Data Layer
    implementation("com.google.android.gms:play-services-wearable:18.2.0")

    // Room for local caching
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines for asynchronous operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.0")
    ```

### 3.2. Data Collection Service (Passive Monitoring)

Since the app will run primarily in the background or via a Tile, a single, power-efficient data collection strategy is required.

* **Passive Mode (`PassiveMonitoringClient`):** A foreground service will host the `PassiveMonitoringClient` to subscribe to `DataType.DAILY_STEPS`. This provides batched, power-efficient updates from the system and is the ideal method for an app that is not always in the user's view.

### 3.3. Smart Update & Data Transmission Logic

The app will transmit step data to the phone based on thresholds. The `step_delta` in the payload will always represent the **new steps since the last transmission**.

* **`StepTrackingService.kt`:**
    * Maintain a running count of "unsynced" steps.
    * **Normal Mode:** When the unsynced step delta reaches **250**, send an update message via the Data Layer with the path `/step_update`.
    * **Battery Saver Mode:** When device battery is < 20%, change the threshold to **1000** steps.
    * After a successful transmission, reset the unsynced step counter.

### 3.4. Local Caching (Room Database)

Implement a Room database to cache incremental step deltas for offline recovery.

* **`CachedStepEntity.kt`:**
    ```kotlin
    @Entity(tableName = "cached_step_deltas")
    data class CachedStepEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val timestamp: Long, // End time of the interval
        val stepDelta: Int,  // Incremental steps for this 15-minute interval
        val synced: Boolean = false
    )
    ```
* **`StepDao.kt`:**
    * `@Insert fun insert(delta: CachedStepEntity)`
    * `@Query("SELECT * FROM cached_step_deltas WHERE synced = :synced ORDER BY timestamp ASC") fun getUnsyncedData(synced: Boolean = false): List<CachedStepEntity>`
    * `@Query("UPDATE cached_step_deltas SET synced = 1 WHERE id IN (:ids)") fun markAsSynced(ids: List<Int>)`
    * `@Query("DELETE FROM cached_step_deltas WHERE timestamp < :cutoffTimestamp") fun pruneOldData(cutoffTimestamp: Long)`
* **Logic:**
    * Every 15 minutes, a `Worker` will commit the current unsynced step delta to the Room database.
    * A separate `Worker` will periodically prune data older than 48 hours.

---

## 4. Phase 2: Phone App (StepBlocks) Integration

### 4.1. Data Reception Service

* **`WearableDataListenerService.kt`:**
    ```kotlin
    class WearableDataListenerService : WearableListenerService() {
        // Inject ViewModel or Repository as needed

        override fun onDataChanged(dataEvents: DataEventBuffer) {
            super.onDataChanged(dataEvents)
            dataEvents.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    if (path == "/step_update") {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val stepDelta = dataMap.getInt("step_delta", 0)
                        
                        // Update UI via ViewModel/Repository
                        // Queue for Health Connect Sync
                    }
                }
            }
        }
    }
    ```

### 4.2. Health Connect Synchronization Logic

This is the most critical part for data integrity. Records written to Health Connect **must** include a `clientRecordId` to enable "upsert" functionality and prevent duplicates.

* **Revised `syncStepsToHealthConnect` Function:**
    ```kotlin
    suspend fun syncStepsToHealthConnect(stepDelta: Int, startTime: Instant, endTime: Instant) {
        // The clientRecordId must be a stable, unique ID for this time interval.
        // e.g., "stepblocks-steps-YYYY-MM-DD-HH-MM"
        val recordId = "stepblocks-steps-${startTime}"

        val stepsRecord = StepsRecord(
            count = stepDelta.toLong(),
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime),
            endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endTime),
            metadata = Metadata.Builder()
                .setDevice(Device.Builder().setManufacturer("StepBlocks").setModel("Pixel Watch").setType(Device.TYPE_WATCH).build())
                .setClientRecordId(recordId)
                .setClientRecordVersion(1) // Increment this version if you later update this same record
                .build()
        )

        try {
            healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            // Implement retry logic with exponential backoff
        }
    }
    ```
* **Initial Baseline Sync Strategy:**
    1.  On first connection, the phone app queries Health Connect for all `StepsRecord` entries for the current day.
    2.  From the results, find the most recent `endTime` across all trusted data sources (e.g., Fitbit, StepBlocks itself). This is the `lastKnownTime`.
    3.  The phone app then sends a message to the watch app requesting a batch sync of all cached step deltas with a `timestamp` greater than `lastKnownTime`.
    4.  This ensures that only new, unrecorded steps are synced, cleanly preventing any double-counting.

### 4.3. UI & State Management

* Use `StateFlow` in a `ViewModel` to hold the real-time step count.
* The `WearableDataListenerService` will update a Repository, which in turn updates the ViewModel's `StateFlow`.
* The UI (Activity/Fragment) will observe the `StateFlow` to update the step count display instantly.
* Implement a visual indicator in the UI to show connection status:
    * **Green:** Connected and receiving real-time data.
    * **Yellow:** Disconnected, showing last known value.
    * **Blue (Pulsing):** Actively syncing a batch of offline data.

---

## 5. Testing & Validation Plan

1.  **Unit Tests:**
    * Test the threshold logic (250 vs 1000 steps) on the watch.
    * Test the Room Database DAO operations.
    * Test the data parsing logic in the `WearableDataListenerService`.
2.  **Integration Tests:**
    * Test the Data Layer communication between the watch and phone emulators.
    * Use Health Connect's synthetic data providers to test the `syncStepsToHealthConnect` logic and verify `clientRecordId` upserts.
3.  **End-to-End Manual Testing:**
    * **Scenario 1 (Online):** Walk with both devices connected and verify near-instant UI updates on the phone.
    * **Scenario 2 (Offline):** Turn off Bluetooth on the phone, walk several hundred steps with the watch, then reconnect and verify that the phone UI updates correctly and a batch sync occurs.
    * **Scenario 3 (Priority):** Ensure both Fitbit and StepBlocks have written data. Verify that by changing the priority in Health Connect settings, the total steps reported by a third-party reader app changes accordingly.

