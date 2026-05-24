# Architecture

## Runtime Flow

1. Wear OS companion observes sleep or activity signals.
2. Watch sends a normalized trigger event to the phone.
3. Phone records the raw trigger event.
4. Trigger engine checks enabled automations and delay settings.
5. Scheduler queues the task for the configured delay.
6. Task executor pauses active media sessions.
7. Event log records the outcome.

The current phone implementation supports the same flow from simulated phone
triggers, a local development broadcast, and Wear OS Data Layer messages. The
Wear app serializes `WearTriggerPayload` through the shared JSON codec and sends
it with `MessageClient` on `/zzzzzz/trigger`. The phone app receives that path in
`PhoneWearMessageService`, then routes the decoded payload through the same
trigger engine used by simulated events.

The local broadcast receiver remains as a development fallback for single-runtime
testing, but paired phone/watch testing should use the Data Layer path.

## Trigger Model

```text
TriggerDefinition
  id
  type: asleep_detected | stood_up_after_wake
  enabled
  delay_seconds
  task_id

TriggerEvent
  id
  trigger_type
  source: wear_health_services | health_connect | simulated
  detected_at
  confidence
  metadata

TaskRun
  id
  trigger_event_id
  task_type
  scheduled_for
  started_at
  completed_at
  status
  error
```

## Media Pause Action

The phone app needs notification listener access to enumerate active media sessions for normal third-party apps. The action should:

- Query active sessions through `MediaSessionManager`.
- Filter sessions with playback state indicating active playback.
- Send pause to each active controller.
- Record the packages affected and any sessions that reject control.

The first implementation records a human-readable task result. Package-level diagnostic history is deferred until the event store schema is moved from SharedPreferences JSON to a structured database.

## Sleep Detection Strategy

Real-time trigger detection should come from the Wear OS companion where supported. Health Connect should be used for completed sleep session history and reconciliation after wake, because finalized sleep sessions may not be available until the session ends.

The current Wear app exposes manual asleep, awake, and stood-up controls for
transport and task-engine validation. Health Services passive monitoring is the
next step for real detection.

## Stand-Up After Wake Strategy

The second trigger should be modeled as a compound trigger:

- A wake transition opens a short monitoring window.
- Movement or posture-like evidence inside that window is evaluated.
- The trigger fires only after a debounce period to avoid false positives.

Initial implementation can use a conservative "wake plus steps/activity transition" rule. Later versions can refine this with watch-specific capabilities.
