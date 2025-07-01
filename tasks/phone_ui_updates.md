# Task: Real-Time UI Updates

**Requirement:** FR2.2 - The app's UI must update instantly to reflect the new step count received from the watch. This will be managed via a `StateFlow` in a ViewModel.

---

## Work Done

- Implemented `_realtimeSteps` StateFlow in `HealthConnectRepository` to hold real-time step count.
- Integrated data flow: `WearableDataListenerService` updates `HealthConnectRepository`, which updates `_realtimeSteps`.
- `TodayViewModel` now collects `_realtimeSteps`.
- UI (`TodayScreen`) observes `totalDailySteps` which is now derived from `_realtimeSteps`, ensuring instant updates.
- Refactored permission handling by moving related properties/functions from `HealthConnectManager` to `HealthConnectRepository` and exposing them via `TodayViewModel`.
- Cleaned up `TodayViewModel` and `TodayScreen` to remove direct `HealthConnectManager` dependencies.

**Status:** Completed
