Task: Robust Error Handling
Objective: Make the data synchronization process resilient to transient network failures or temporary unavailability of Health Connect.

Functional Requirements
1. Implement Retry Logic
   Requirement: FR-B.1

File to Modify: app/src/main/java/com/stepblocks/repository/HealthConnectRepository.kt

Action: Update the try-catch block within the syncStepsToHealthConnect function to include a retry mechanism instead of the current // TODO comment.

2. Use Exponential Backoff
   Requirement: FR-B.2

Action:

The retry mechanism must use an exponential backoff strategy.

Initial Delay: If the healthConnectClient.insertRecords call fails, wait for an initial duration (e.g., 2 seconds).

Subsequent Delays: If the operation fails again, double the delay before the next retry (e.g., 4 seconds, 8 seconds, 16 seconds).

Maximum Delay: Cap the delay at a reasonable maximum (e.g., 60 seconds) to avoid excessively long waits.

Max Retries: Stop retrying after a certain number of attempts (e.g., 5 retries) to avoid indefinite loops.