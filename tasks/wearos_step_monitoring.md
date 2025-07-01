# Task: Background Step Monitoring

**Requirement:** FR1.1 - The app must run a foreground service on the watch to subscribe to `DataType.DAILY_STEPS` using the `PassiveMonitoringClient` from Health Services.

---

## Work Done

*   Created the `StepTrackingService.kt` file in `com.stepblocks.wear.services`.
*   The service uses `HealthServices.getClient()` to get a `PassiveMonitoringClient`.
*   A `PassiveListenerCallback` is implemented to handle incoming step data.
*   In `onStartCommand`, the service registers the callback for `DataType.DAILY_STEPS`.
*   Updated the `AndroidManifest.xml` to include the `StepTrackingService`.
*   Added the `ACTIVITY_RECOGNITION` and `FOREGROUND_SERVICE` permissions to the manifest.
*   The project builds successfully with these changes.
