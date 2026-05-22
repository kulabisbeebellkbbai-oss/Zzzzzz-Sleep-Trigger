# Project Plan

## Goal

Build an Android app with a Wear OS companion that runs designated tasks from sleep and wake-related triggers. The first production task is to pause media playback and record when the action happened. The first user-configurable delay is "pause media 5 minutes after sleep is detected."

## Product Requirements

- Let the user connect a compatible Wear OS watch.
- Detect an asleep state from the watch.
- Support a delay between trigger detection and task execution.
- Pause currently active phone media sessions after the delay.
- Record event time, trigger source, configured delay, task result, and any error.
- Detect a wake transition and then trigger when the user stands up or begins moving.
- Keep all automation user-approved and visible in settings.

## Milestones

### 1. Documentation and Baseline

- Initialize Android phone and Wear OS modules.
- Document platform dependencies, required permissions, architecture, and testing.
- Publish the initial public repository.

### 2. Local Trigger Engine

- Add a local database for trigger definitions and event history. Partially complete with a SharedPreferences-backed event log.
- Add a simulated sleep trigger for development. Complete.
- Add a delayed task scheduler. Complete with platform `AlarmManager`.
- Add the "pause all active media" task. Complete through `MediaSessionManager`, pending real-device permission verification.

### 3. Wear OS Sleep Trigger

- Add Wear OS Health Services passive monitoring integration.
- Send sleep trigger events from watch to phone.
- Add fallback/manual simulation controls for devices that do not expose real-time sleep events.

### 4. Health Connect History

- Request Health Connect sleep-read permission.
- Import completed sleep sessions for audit, summaries, and trigger reconciliation.
- Reconcile watch-side trigger time with finalized sleep-session data after wake.

### 5. Stand-Up After Wake Trigger

- Detect wake transition from watch-side or finalized sleep data.
- Watch for stand-up or movement after wake using wearable sensor/activity signals and phone-side Activity Recognition where useful.
- Add a configurable task slot for this trigger.

### 6. Reliability and Release Readiness

- Add onboarding for notification access, Health Connect, activity recognition, and watch pairing.
- Add battery optimization guidance only where required.
- Add unit, integration, and device tests.
- Prepare privacy policy, Play Store disclosures, and release signing.

## Key Risks

- Real-time sleep detection availability varies by watch, Wear OS version, and vendor implementation.
- Pausing media from other apps requires notification listener access or equivalent media-session control privileges.
- Exact timing after sleep may be affected by Android background execution and battery policy.
- Health and sleep data is sensitive; the app should default to local processing and minimal retention.

## Next Development Step

Implement milestone 3: a Wear OS signal bridge that can send normalized watch-side sleep and wake events to the phone trigger engine.
