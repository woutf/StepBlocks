# Product Requirements Document: StepBlocks Real-Time Watch Integration

## 1. Overview

This document outlines the product and functional requirements for a Wear OS companion app for StepBlocks. The primary objective is to provide users with a real-time view of their step count by creating a direct data channel between their watch and the StepBlocks phone app, bypassing the inherent latency of the standard Health Connect sync cycle.

**Target Audience:** All StepBlocks users with a compatible Wear OS 3+ watch.

**User Problem:** Users experience a significant delay between the moment they take steps and when those steps are reflected in the StepBlocks app. This is due to the infrequent sync schedule of Health Connect, which can lead to confusion and a disconnected user experience. Users want immediate, real-time feedback on their physical activity.

## 2. Goals & Objectives

*   **Provide Real-Time Data:** Eliminate step count latency by displaying data from the watch on the phone app in near real-time.
*   **Ensure Data Integrity:** Guarantee that all step data is accurately and reliably synced to Health Connect, preventing data loss or duplication.
*   **Deliver a Seamless User Experience:** Create a "set it and forget it" experience where the watch and phone app work together automatically in the background.
*   **Maintain Battery Efficiency:** Build a power-efficient solution on the watch that does not excessively drain the battery.

## 3. Functional Requirements

### 3.1. Wear OS Companion App (MVP)

The watch app will be the primary source for real-time step data.

*   **FR1.1 - Background Step Monitoring:** The app must run a foreground service on the watch to subscribe to `DataType.DAILY_STEPS` using the `PassiveMonitoringClient` from Health Services.
*   **FR1.2 - Intelligent Data Transmission:** The app must transmit the step delta (new steps since the last send) to the phone app.
    *   **Normal Mode:** Send an update when the unsynced step delta reaches 250 steps.
    *   **Power Saver Mode:** Change the threshold to 1000 steps when the watch battery is below 20%.
*   **FR1.3 - Local Data Caching:** The app must cache incremental step deltas locally using a Room database.
    *   A `Worker` will save the current unsynced step count to the database every 15 minutes.
    *   Cached data must be marked as "synced" after successful transmission to the phone.
*   **FR1.4 - Data Pruning:** A `Worker` must periodically delete cached step data that is older than 48 hours to manage storage.

### 3.2. Phone App (StepBlocks) Integration (MVP)

The phone app will listen for data from the watch, update the UI, and sync with Health Connect.

*   **FR2.1 - Data Reception:** The app must implement a `WearableListenerService` to receive `/step_update` data messages from the watch.
*   **FR2.2 - Real-Time UI Updates:** The app's UI must update instantly to reflect the new step count received from the watch. This will be managed via a `StateFlow` in a ViewModel.
*   **FR2.3 - Health Connect Synchronization:** The app is responsible for writing all step data to Health Connect.
    *   All records written to Health Connect **must** include a stable, unique `clientRecordId` (e.g., `stepblocks-steps-YYYY-MM-DD-HH-MM`) to prevent data duplication.
    *   The record's metadata must correctly identify the data source as the watch.
*   **FR2.4 - Initial Baseline Sync:**
    *   On the first connection, the phone app must query Health Connect to find the last known sync time from all trusted sources.
    *   The phone must then request a batch sync from the watch for all data recorded after this `lastKnownTime` to prevent double-counting.
*   **FR2.5 - Connection Status Indicator:** The UI must display the connection status to the watch:
    *   **Green:** Connected and receiving real-time data.
    *   **Yellow:** Disconnected, showing the last known value.
    *   **Blue (Pulsing):** Actively syncing a batch of historical data.

### 3.3. Error Handling

*   **FR3.1 - User-Facing Error Messages:** All user-facing errors (e.g., connection loss, sync failures) will be communicated via Toast messages within the app.

## 4. Out of Scope (for this version)

*   **Watch App User Interface:** The initial version will not have a dedicated user interface or a Tile on the watch, operating solely as a background service.
*   **Advanced Configuration:** There will be no user-configurable settings for sync frequency or battery thresholds in this version.
*   **Detailed Error Recovery UI:** Beyond Toast messages, there will be no complex UI for guiding users through error recovery steps.

## 5. Assumptions

*   Users have a Wear OS 3 (or higher) compatible watch.
*   Users have already granted StepBlocks the necessary permissions for Health Connect on the phone.
*   The watch and phone are paired and have Bluetooth enabled for the Data Layer to function.
