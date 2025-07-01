Task: Initial Baseline & Historical Data Sync
Objective: Implement a historical data sync mechanism to prevent data gaps when a user connects their watch for the first time or after a prolonged disconnection.

Functional Requirements
1. Triggering the Sync (Phone App)
   Requirement: FR-A.1

Action: In the WearableDataListenerService, within the onPeerConnected function, initiate the historical sync process.

2. Requesting Historical Data (Phone App)
   Requirement: FR-A.2

Action:

Call the healthConnectRepository.getLastKnownTime() function to get the timestamp of the last known step record in Health Connect.

Create and send a new message to the watch via the Wearable Data Layer.

Path: /request_historical_data

Payload: The lastKnownTime timestamp.

3. Handling the Request (Watch App)
   Requirement: FR-A.3

Action:

The watch's WearableDataListenerService must be updated to listen for the /request_historical_data path.

When the message is received, extract the lastKnownTime from the payload.

Query the local Room database using the StepDao to get all CachedStepEntity records where the timestamp is greater than lastKnownTime.

Package the list of resulting CachedStepEntity objects into a data payload.

4. Transmitting Historical Data (Watch App)
   Requirement: FR-A.3

Action:

Send a message back to the phone containing the packaged historical data.

Path: /historical_data_response

Payload: The list of unsynced CachedStepEntity objects.

5. Receiving and Finalizing the Sync (Phone App)
   Requirement: FR-A.4

Action:

The phone's WearableDataListenerService must be updated to handle the /historical_data_response path.

Set the connection status in the HealthConnectRepository to ConnectionStatus.Syncing.

Parse the incoming list of CachedStepEntity objects.

For each entity, call healthConnectRepository.syncStepsToHealthConnect() to write the historical data to Health Connect.

Once all records have been processed, set the connection status back to ConnectionStatus.Connected.