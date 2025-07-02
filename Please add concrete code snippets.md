<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

# Please add concrete code snippets

Here are **concrete code snippets** and integration steps to update your phone app for the new watch sync model. This covers:

- Receiving `/steps_update` messages with the current daily step total
- Calculating the delta using Room (`DailyProgress`)
- Syncing the delta to Health Connect
- Resetting the baseline at local midnight
- Removing historical sync logic


## 1. **Extend DailyProgress Entity**

Add a field to store the last received step total for the day.

```kotlin
// DailyProgress.kt
@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey val date: Date,
    val templateId: String,
    val blockProgress: List<BlockProgress>,
    val lastStepTotal: Long = 0L // <-- Add this field
)
```


## 2. **Update DailyProgressDao**

Add/update methods to get and set the last step total for a date.

```kotlin
// DailyProgressDao.kt
@Dao
interface DailyProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyProgress(dailyProgress: DailyProgress)

    @Update
    suspend fun updateDailyProgress(dailyProgress: DailyProgress)

    @Query("SELECT * FROM daily_progress WHERE date = :date")
    suspend fun getDailyProgressByDate(date: Date): DailyProgress?
}
```


## 3. **Handle Step Updates in WearableDataListenerService**

Replace your `/step_update` and `/historical_data_response` logic with the following:

```kotlin
// WearableDataListenerService.kt
override fun onMessageReceived(messageEvent: MessageEvent) {
    if (messageEvent.path == "/steps_update") {
        val newTotal = String(messageEvent.data).toLongOrNull() ?: return
        serviceScope.launch {
            val today = LocalDate.now()
            val date = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val db = AppDatabase.getDatabase(applicationContext)
            val dailyProgressDao = db.dailyProgressDao()

            // Get or create today's DailyProgress
            val dailyProgress = dailyProgressDao.getDailyProgressByDate(date)
                ?: DailyProgress(date = date, templateId = "", blockProgress = emptyList(), lastStepTotal = 0L)

            val lastTotal = dailyProgress.lastStepTotal
            val delta = (newTotal - lastTotal).coerceAtLeast(0L)

            if (delta > 0) {
                val now = Instant.now()
                healthConnectRepository.updateConnectionStatus(ConnectionStatus.Syncing)
                healthConnectRepository.syncStepsToHealthConnect(delta.toInt(), now, now)
                healthConnectRepository.updateRealtimeSteps(newTotal)
                // Update Room with new total
                dailyProgressDao.insertDailyProgress(
                    dailyProgress.copy(lastStepTotal = newTotal)
                )
                healthConnectRepository.updateConnectionStatus(ConnectionStatus.Connected)
            } else if (lastTotal == 0L) {
                // First run or midnight reset
                healthConnectRepository.updateRealtimeSteps(newTotal)
                dailyProgressDao.insertDailyProgress(
                    dailyProgress.copy(lastStepTotal = newTotal)
                )
            }
        }
    }
}
```


## 4. **Reset Baseline at Midnight**

On app start or after midnight, ensure a new `DailyProgress` is created:

```kotlin
// In your Application or a startup routine
val today = LocalDate.now()
val date = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
val db = AppDatabase.getDatabase(context)
val dailyProgressDao = db.dailyProgressDao()
val dailyProgress = dailyProgressDao.getDailyProgressByDate(date)
if (dailyProgress == null) {
    dailyProgressDao.insertDailyProgress(
        DailyProgress(date = date, templateId = "", blockProgress = emptyList(), lastStepTotal = 0L)
    )
}
```

You can run this check in your ViewModel’s `loadData()` or in a background worker at midnight.

## 5. **Remove Legacy and Historical Sync Code**

- Delete `/step_update` DataMap handling in `onDataChanged`.
- Delete `/historical_data_response` and `/request_historical_data` logic in `WearableDataListenerService.kt`.
- Remove any related code in your repository and UI.


## 6. **HealthConnectRepository: No Change Needed**

Your method:

```kotlin
suspend fun syncStepsToHealthConnect(stepDelta: Int, startTime: Instant, endTime: Instant)
```

is already correct for writing deltas to Health Connect[^1].

## 7. **UI and ViewModel**

No changes needed if you already observe `realtimeSteps` in your `TodayViewModel` and display it in `TodayScreen`. The new total will be reflected automatically.

## 8. **Summary Table**

| Area | Change/Action |
| :-- | :-- |
| Room | Add `lastStepTotal` to `DailyProgress`; update DAO methods |
| Message Handler | Parse `/steps_update`, calculate delta, write to Health Connect, update Room |
| Baseline Reset | On app launch/midnight, ensure new `DailyProgress` with `lastStepTotal = 0L` |
| Legacy Code | Remove all historical/data map sync logic |
| UI/ViewModel | No change if observing `realtimeSteps` |

## 9. **Testing**

- Simulate step updates from the watch.
- Restart the app after midnight and verify a new `DailyProgress` is created.
- Confirm Health Connect and the UI show correct, up-to-date step totals.

If you need a **full file rewrite** or further integration details for any specific file, let me know!

<div style="text-align: center">⁂</div>

[^1]: HealthConnectRepository.kt

[^2]: MainActivity.kt

[^3]: StepBlocksApplication.kt

[^4]: WearableDataListenerService.kt

[^5]: TodayScreen.kt

[^6]: TodayViewModel.kt

[^7]: AppDatabase.kt

[^8]: BlockProgress.kt

[^9]: BlockProgressDao.kt

[^10]: Converters.kt

[^11]: DailyProgress.kt

[^12]: DailyProgressDao.kt

[^13]: DailyProgressWithBlockProgress.kt

[^14]: DailyProgressWithBlocks.kt

[^15]: HealthConnectManager.kt

[^16]: Converters.kt

[^17]: OfflineTemplateRepository.kt

[^18]: TemplateRepository.kt

