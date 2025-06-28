# Implementation Plan: StepBlocks

This document outlines the development plan for completing the StepBlocks application, based on the revised `description.txt` and `technical_steps.txt`.

## Phase 1: History & Live Data Foundation (Next Steps)

This phase focuses on creating the necessary data structures for historical tracking and integrating with Health Connect.

### Task 1.1: Create History Data Models

*   **Goal:** Implement the Room entities and DAOs for `DailyProgress` and `BlockProgress`.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/data/DailyProgress.kt`: Create this data class with fields for `date`, `templateId`, and a list of `BlockProgress` objects.
    *   `app/src/main/java/com/stepblocks/data/BlockProgress.kt`: Create this data class with fields for `blockName`, `actualSteps`, `targetSteps`, and notification status booleans.
    *   `app/src/main/java/com/stepblocks/data/Converters.kt`: Add `TypeConverter` functions to handle the serialization/deserialization of the `List<BlockProgress>`.
    *   `app/src/main/java/com/stepblocks/data/DailyProgressDao.kt`: Create this DAO with basic CRUD operations.
    *   `app/src/main/java/com/stepblocks/data/BlockProgressDao.kt`: Create this DAO with basic CRUD operations.
    *   `app/src/main/java/com/stepblocks/data/AppDatabase.kt`: Update the `@Database` annotation to include the new entities (`DailyProgress.class`, `BlockProgress.class`).

### Task 1.2: Implement Health Connect Step Reader

*   **Goal:** Create the function to read step data from Health Connect.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/data/HealthConnectManager.kt`: Implement a function `readSteps(startTime: Instant, endTime: Instant): Long?` that returns the step count for a given time range. Include error handling for permission denial.

## Phase 2: "Today" Screen

This phase will build the main user-facing screen for live progress tracking.

### Task 2.1: Create "Today" Screen ViewModel

*   **Goal:** Implement the `ViewModel` for the `TodayScreen`.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/ui/screens/TodayViewModel.kt`: Create this `ViewModel`. It will:
        *   Use `DayAssignmentDao` to find the current day's template.
        *   Use the current time to identify the active time block.
        *   Use `HealthConnectManager` to fetch total steps for the day and the current block.
        *   Use `DailyProgressDao` to fetch or create today's progress data.
        *   Expose a `StateFlow` of `TodayScreenUiState` containing all the necessary data for the UI.

### Task 2.2: Create "Today" Screen UI

*   **Goal:** Implement the Composable UI for the `TodayScreen`.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/ui/screens/TodayScreen.kt`: Create this Composable screen. It will:
        *   Observe the `UiState` from the `TodayViewModel`.
        *   Display circular progress bars for daily and current block progress.
        *   Show text for step counts, percentages, and pace status.
        *   Be added to the `AppNavigation.kt` as the new start destination.

## Phase 3: History Screen

This phase will allow users to view their past performance.

### Task 3.1: Create History Screen ViewModel

*   **Goal:** Implement the `ViewModel` for the `HistoryScreen`.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/ui/screens/HistoryViewModel.kt`: Create this `ViewModel`. It will:
        *   Use `DailyProgressDao` to fetch all `DailyProgress` records.
        *   Expose a `StateFlow` of `HistoryScreenUiState`.

### Task 3.2: Create History Screen UI

*   **Goal:** Implement the Composable UI for the `HistoryScreen`.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/ui/screens/HistoryScreen.kt`: Create this Composable screen. It will:
        *   Display a calendar view.
        *   Show a summary of completion percentage on each day in the calendar.
        *   Display a detailed view of `BlockProgress` for the selected day.
        *   Be added to `AppNavigation.kt`.

## Phase 4: Background Processing & Notifications

This phase will add proactive notifications to the app.

### Task 4.1: Schedule Background Work

*   **Goal:** Create a `WorkManager` worker and the logic to schedule it.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/workers/StepBlocksWorker.kt`: Create this worker class.
    *   `app/src/main/java/com/stepblocks/workers/StepBlocksScheduler.kt`: Create a class with a function to schedule the worker to run at the start, midpoint, and end of each time block for the current day's template.

### Task 4.2: Implement Notification Logic

*   **Goal:** Implement the `doWork()` method of the worker.
*   **Files to Create/Modify:**
    *   `app/src/main/java/com/stepblocks/workers/StepBlocksWorker.kt`: Implement the `doWork()` method to:
        *   Fetch the latest step count from `HealthConnectManager`.
        *   Update the `BlockProgress` in the database.
        *   Show a system notification with the user's progress.

## Phase 5: Polishing and Finalization

This phase includes final touches and refinements.

*   **Task 5.1:** Add loading indicators and empty states to all screens.
*   **Task 5.2:** Thoroughly test notification scheduling and delivery.
*   **Task 5.3:** Ensure the UI is responsive and handles edge cases gracefully.
