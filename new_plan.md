<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

# Before we do that, can you write a PRD for this new version of the app I can hand off to an external developer if needed.

**Product Requirements Document (PRD): Pixel Watch Step Sync App (Simplified Version)**

**Overview**
This document defines the requirements for a new, simplified version of the Pixel Watch step tracking app. The app’s primary function is to track the user’s daily steps using the watch’s built-in sensors and send the current daily total to the paired phone app every 250 steps. The phone app handles all historical processing and Health Connect integration. The watch app also provides a basic UI displaying the current step count.

### 1. Goals

- **Accurate, near real-time step tracking** on the phone by sending updates every 250 steps.
- **Minimal complexity** on the watch: no local caching, history, or delta logic.
- **Robustness**: Handles connection interruptions gracefully by always sending the latest daily total.
- **Simple, user-friendly UI**: Displays current step count on the watch.


### 2. Functional Requirements

#### 2.1 Step Tracking

- Use the **watch’s built-in step sensor** via the Android Health Services API.
- Register a **passive goal** to wake the app and trigger an update every 250 steps[^1][^2].
- Track the **current day’s step total** only.


#### 2.2 Data Sync

- On every 250-step increment, **send the current daily step total** to the paired phone app.
- Use the **Wear OS DataLayer API** (MessageClient or DataItem) for communication.
- If the watch is offline, resume sending the latest total upon reconnection.


#### 2.3 Phone App Integration

- The phone app receives the step total, calculates the delta, and writes updates to Health Connect[^1][^2].
- No historical or delta logic is required on the watch.


#### 2.4 User Interface

- The main view displays the **current live step count**.
- Show a minimal, modern UI (Jetpack Compose preferred).
- Optional: Show last sync status or error if sync fails.


#### 2.5 Background Operation

- The app must function reliably in the background, using passive monitoring to minimize battery impact.
- Ensure compliance with Wear OS background execution limits.


### 3. Non-Functional Requirements

- **Battery Efficiency:** Use passive monitoring and avoid unnecessary wakeups.
- **Reliability:** Ensure step data is not lost during disconnections; always send the latest total on reconnect.
- **Maintainability:** Code should be modular, well-documented, and easy to extend.
- **Privacy:** Only step count data is collected and transmitted; no sensitive user data.


### 4. Out of Scope

- No local storage of historical step data on the watch.
- No time-block or hourly breakdowns.
- No direct writes to Health Connect from the watch.
- No advanced analytics or charts on the watch.


### 5. Technical Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Sensors:** Android Health Services API (passive monitoring)
- **Communication:** Wear OS DataLayer API


### 6. Acceptance Criteria

- The watch app reliably tracks steps and sends the current total to the phone every 250 steps.
- The phone app receives updates and accurately reflects the user’s step count in near real time.
- The watch UI displays the current step count and basic sync status.
- The app works reliably across connection interruptions and resumes updates automatically.

If you need wireframes, technical diagrams, or additional UX notes, let me know and I can include those as well[^3].

<div style="text-align: center">⁂</div>

[^1]: programming.health_connect

[^2]: programming.health_tracking

[^3]: skills.ux_design

