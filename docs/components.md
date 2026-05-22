# Required Components

## Android Phone App

- Settings UI for triggers, delays, task selection, permissions, and event history.
- Trigger/task engine that maps trigger events to delayed actions.
- Scheduler for delayed execution. WorkManager is acceptable for flexible timing; AlarmManager may be needed when the user expects tighter timing.
- Event store for sleep trigger time, scheduled execution time, actual execution time, action result, and error details.
- Notification listener service for access to active media sessions from other apps.
- Media control adapter using `MediaSessionManager` and media controllers to pause active sessions.

## Wear OS Companion

- Passive monitoring service for sleep-related and movement-related signals.
- Watch-to-phone event transport.
- Lightweight status UI for pairing, permission state, and diagnostics.
- Device capability detection because Health Services support varies by watch.

## Health Data Integrations

- Wear OS Health Services for real-time wearable events where supported.
- Health Connect for completed sleep session records and cross-app sleep history.
- Permissions for sleep reads, activity recognition, and relevant sensor data.

## Wake and Stand-Up Detection

- Wake event source from the watch-side sleep detector or from completed sleep data when real-time wake is unavailable.
- Movement/stand-up heuristic after wake, based on wearable passive monitoring, step/activity transitions, or phone Activity Recognition.
- Debounce rules so rolling over in bed does not count as standing up.

## Task System

- Initial task: pause active media sessions.
- Task result log with timestamps and errors.
- Future task extension points for alarms, smart-home webhooks, notifications, or app intents.
- User-controlled enable/disable switch per trigger.

## Permissions and User Grants

- Health Connect sleep-read grant.
- Wear OS body sensor/activity permissions as required by selected signals.
- Android activity recognition permission.
- Notification access grant for controlling active media sessions.
- Notification permission for foreground/status notifications on Android 13 and newer.

## External Platform References

- Health Connect sleep sessions: https://developer.android.com/health-and-fitness/health-connect/features/sleep-sessions
- Health Connect sleep experiences: https://developer.android.com/health-and-fitness/health-connect/experiences/sleep
- Wear OS Health Services: https://developer.android.com/health-and-fitness/guides/health-services
- Activity Recognition Transition API: https://developer.android.com/develop/sensors-and-location/location/transitions
- MediaSessionManager: https://developer.android.com/reference/android/media/session/MediaSessionManager

