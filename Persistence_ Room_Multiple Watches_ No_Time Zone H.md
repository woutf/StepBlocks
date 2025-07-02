<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

# Persistence: Room

Multiple Watches: No
Time Zone Handling: whatever is best
Historical Sync: remove

Here’s a **detailed document** outlining the required changes for your phone app to correctly receive and process step data from the simplified watch app. This plan is tailored to your requirements:

- **Persistence:** Use Room for storing the last step total
- **Multiple Watches:** Not needed
- **Time Zone Handling:** Use local midnight as the reset point (best practice for step tracking)
- **Historical Sync:** Remove all related code


## 1. **Overview of Required Changes**

- **Receive the current daily step total** from the watch via a `/steps_update` message.
- **Persist the last received step total** in your Room database (e.g., in a new table or as part of your DailyProgress entity).
- **Calculate the delta** between the new total and the last stored value.
- **Write the delta to Health Connect** if it’s positive.
- **Update the UI and in-app state** with the new total.
- **Reset the baseline at local midnight** to handle day changes.


## 2. **Detailed Change List**

### A. **Update Data Reception Logic**

**File:** `WearableDataListenerService.kt`

- **Remove:** All `/step_update` DataMap and `/historical_data_response` logic.
- **Add:** Handle `/steps_update` in `onMessageReceived` to parse the current daily step total.

**Example:**

```kotlin
override fun onMessageReceived(messageEvent: MessageEvent) {
    if (messageEvent.path == "/steps_update") {
        val newTotal = String(messageEvent.data).toLongOrNull() ?: return
        serviceScope.launch {
            val today = LocalDate.now()
            val dailyProgressDao = AppDatabase.getDatabase(applicationContext).dailyProgressDao()
            val dailyProgress = dailyProgressDao.getDailyProgressByDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            val lastTotal = dailyProgress?.let { it.blockProgress.sumOf { bp -> bp.actualSteps } } ?: 0L

            val delta = if (newTotal > lastTotal) newTotal - lastTotal else 0L

            if (delta > 0) {
                val now = Instant.now()
                healthConnectRepository.syncStepsToHealthConnect(delta.toInt(), now, now)
                healthConnectRepository.updateRealtimeSteps(newTotal)
                // Update Room with the new total
                updateDailyProgress(newTotal, today)
            } else if (lastTotal == 0L) {
                healthConnectRepository.updateRealtimeSteps(newTotal)
                updateDailyProgress(newTotal, today)
            }
        }
    }
}
```

**Note:**

- You may want to create a helper in your DAO to store the last received step total for each day.
- If you use a dedicated column for the last total, update it here.


### B. **Room Persistence for Step Totals**

**File:** `DailyProgress.kt`, `DailyProgressDao.kt`

- **Add:** A field for the last received step total in your `DailyProgress` entity, or store it as a special block in `blockProgress`.
- **Add/Update:** DAO methods to get and set today’s last step total.

**Example:**

```kotlin
@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey val date: Date,
    val lastStepTotal: Long = 0L, // <-- Add this field
    // ... other fields
)
```

**In your DAO:**

```kotlin
@Query("SELECT * FROM daily_progress WHERE date = :date")
suspend fun getDailyProgressByDate(date: Date): DailyProgress?

@Update
suspend fun updateDailyProgress(dailyProgress: DailyProgress)
```

**Helper function:**

```kotlin
suspend fun updateDailyProgress(newTotal: Long, today: LocalDate) {
    val dao = AppDatabase.getDatabase(applicationContext).dailyProgressDao()
    val date = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
    val progress = dao.getDailyProgressByDate(date)
    if (progress != null) {
        dao.updateDailyProgress(progress.copy(lastStepTotal = newTotal))
    } else {
        dao.insertDailyProgress(DailyProgress(date = date, lastStepTotal = newTotal))
    }
}
```


### C. **Reset Baseline at Midnight**

- On app launch or after midnight, check if the date has changed.
- If so, create a new `DailyProgress` record for the new day with `lastStepTotal = 0L`.


### D. **Remove Historical Sync Code**

- Remove all `/historical_data_response` and `/request_historical_data` logic from `WearableDataListenerService.kt` and related code in the repository.


### E. **UI and ViewModel**

**Files:** `TodayViewModel.kt`, `TodayScreen.kt`

- No change needed if you already observe `realtimeSteps`.
- If you want to show the last sync time, update a field in your ViewModel when new data is received.


## 3. **Summary Table**

| Area | Action |
| :-- | :-- |
| Data Reception | Parse `/steps_update` message, extract daily total, calculate delta |
| Room Persistence | Store last received step total in `DailyProgress` (add field if needed) |
| Health Connect Sync | Write only positive deltas to Health Connect |
| UI Updates | Update `realtimeSteps` StateFlow, UI observes as before |
| Baseline Reset | At midnight, create new `DailyProgress` with `lastStepTotal = 0L` |
| Remove Legacy/History | Delete all historical sync code and DataMap handling |

## 4. **Questions Answered**

- **Persistence:** Room (`DailyProgress` table, new field)
- **Multiple Watches:** No changes needed
- **Time Zone Handling:** Use local midnight for baseline reset
- **Historical Sync:** Remove all related code


## 5. **Final Notes**

- Make sure your `WearableDataListenerService` is registered in your `AndroidManifest.xml` with the correct intent filters for `com.google.android.gms.wearable.BIND_LISTENER`.
- Test the full flow: new day, reconnection, app restarts, and large step jumps.
- If you want to support legacy watches, you can keep the old DataMap logic as a fallback, but this is optional.

Let me know if you want **concrete code snippets** for any of these steps or have questions about Room integration details.

<div style="text-align: center">⁂</div>

[^1]: MainActivity.kt

[^2]: StepBlocksApplication.kt

[^3]: WearableDataListenerService.kt

[^4]: TodayScreen.kt

[^5]: TodayViewModel.kt

[^6]: AppDatabase.kt

[^7]: BlockProgress.kt

[^8]: BlockProgressDao.kt

[^9]: Converters.kt

[^10]: DailyProgress.kt

[^11]: DailyProgressDao.kt

[^12]: DailyProgressWithBlockProgress.kt

[^13]: DailyProgressWithBlocks.kt

[^14]: HealthConnectManager.kt

[^15]: Converters.kt

[^16]: HealthConnectRepository.kt

[^17]: OfflineTemplateRepository.kt

[^18]: TemplateRepository.kt

[^19]: https://discussions.apple.com/thread/252936299

[^20]: https://code.tutsplus.com/get-wear-os-and-android-talking-exchanging-information-via-the-wearable-data-layer--cms-30986t

[^21]: https://codezup.com/androids-room-persistence-library-a-comprehensive-tutorial/

[^22]: https://androidaps.readthedocs.io/en/3.1/Configuration/Watchfaces.html

[^23]: https://developer.android.com/health-and-fitness/guides/basic-fitness-app/integrate-wear-os

[^24]: https://github.com/LarkspurCA/androidweardocs/blob/master/sync.rst

[^25]: https://www.firstnet.com/help/device-help/numbersync-for-wearables/numbersync-for-google/numbersync-unsync-for-google.html

[^26]: https://blog.csdn.net/hnjzfwy/article/details/134916306

[^27]: https://support.modernhealth.com/hc/en-us/articles/5476675454107-Time-Zone-Scheduling-Options

[^28]: https://docs.junction.com/wearables/guides/android-health-connect

[^29]: https://www.reddit.com/r/ios/comments/abjz8d/is_there_a_way_to_find_out_how_many_steps_i_took/

[^30]: https://support.apple.com/en-us/108779

[^31]: https://www.youtube.com/watch?v=4X8XTlOsAtI

[^32]: https://stackoverflow.com/questions/61717974/reading-daily-step-count-on-android-wear

[^33]: https://stackoverflow.com/questions/79411456/how-to-filterout-manual-step-count-from-health-connect

[^34]: https://www.youtube.com/watch?v=OopjNYlqTQ4

[^35]: https://stackoverflow.com/questions/25430232/android-wear-deleting-data-on-dataapi-with-deletedataitems

[^36]: https://www.reddit.com/r/WearOS/comments/j0i1ug/stuck_on_getting_your_watch_details/

[^37]: https://gist.github.com/henrikfroehling/5d9e290ae002e87667ea3f1aa2747cbc

[^38]: https://www.youtube.com/watch?v=__UbmZRD7rY

[^39]: https://www.simoahava.com/gtm-tips/remember-to-flush-unused-data-layer-variables/

[^40]: https://developers.google.com/android/reference/com/google/android/gms/wearable/DataClient?authuser=0000

[^41]: https://source.android.com/docs/core/permissions/timezone-rules

[^42]: https://source.android.com/docs/core/ota/modular-system/timezone

[^43]: https://stackoverflow.com/questions/78024890/how-should-i-handle-timezones-in-this-context

[^44]: https://www.tinybird.co/blog-posts/database-timestamps-timezones

[^45]: https://android.googlesource.com/platform/system/timezone/+/refs/heads/android13-qpr2-b-s1-release/README.android

[^46]: https://community.fitbit.com/t5/Third-Party-Integrations/Wrong-time-for-entries-sent-to-Health-Connect/td-p/5574855

[^47]: https://www.10000steps.org.au/support/mobile-app-support/device-android-health-connect/

[^48]: https://www.w3.org/TR/timezone/

