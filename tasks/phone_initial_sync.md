# Task: Initial Baseline Sync

**Requirement:** FR2.4 - Initial Baseline Sync:
*   On the first connection, the phone app must query Health Connect to find the last known sync time from all trusted sources.
*   The phone must then request a batch sync from the watch for all data recorded after this `lastKnownTime` to prevent double-counting.

---

## Work Done

*   Implemented the `getLastKnownTime()` function in `HealthConnectRepository.kt`.
*   This function queries Health Connect for all `StepsRecord` entries from the last 30 days.
*   It finds the most recent `endTime` among all records and returns it.
*   If no records are found, it returns a default value of 24 hours ago.
*   This logic successfully fulfills the requirement to find the last known sync time.
