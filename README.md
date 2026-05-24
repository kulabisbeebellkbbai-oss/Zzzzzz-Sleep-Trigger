# Zzzzzz Sleep Trigger

Zzzzzz Sleep Trigger is an Android and Wear OS app concept for running user-approved tasks when a smartwatch detects sleep and wake-related movement.

The first task is:

- Detect that the user has fallen asleep.
- Wait for the configured delay, such as 5 minutes.
- Pause active media playback on the phone.
- Record the event time and task result locally.

The second trigger is:

- Detect that the user has woken from a sleep session.
- Fire a trigger when the person stands up or begins moving after waking.

## Initial Scope

This repository starts with a phone app, Wear OS companion, shared payload
module, and local test media target:

- `app/`: phone app, task configuration, media control, event log, and user permissions.
- `wear/`: Wear OS companion, real-time sleep and activity detection bridge.
- `shared/`: trigger payload model and JSON codec shared by phone and watch code.
- `testmedia/`: local MediaSession target used to verify pause behavior.
- `docs/`: product plan, component map, architecture notes, privacy model, and testing plan.

## Important Platform Constraint

Health Connect is useful for reading completed sleep records, but Android's own guidance says sleep sessions are normally written after the session has finished. Real-time "I just fell asleep" automation therefore needs a watch-side detection path, planned here through Wear OS Health Services passive monitoring, with Health Connect as the historical record and compatibility layer.

## Documentation

- [Project plan](docs/project-plan.md)
- [Required components](docs/components.md)
- [Architecture](docs/architecture.md)
- [Privacy and permissions](docs/privacy-security.md)
- [Testing plan](docs/testing.md)

## Current Status

The project is initialized with Android phone and Wear OS modules plus planning documentation. The phone app now includes the first local trigger/task path: a simulated asleep trigger, configurable delayed execution for the first task, media pause through active media sessions, and a local event log.

The core behavior is split into testable trigger, routing, scheduling, and task execution layers. The app also has a default route for the second trigger, stood up after wake, so watch or phone movement events can schedule a task without changing the task engine.

The phone app has a native dashboard UI for automation controls, permission state, and recent events. The Wear OS app has a compact trigger/status UI for asleep, awake, and stood-up events, and sends normalized payloads to the phone receiver.

The next implementation milestone is replacing the current local development broadcast transport with real Wear OS Data Layer or MessageClient transport, then adding Health Services passive monitoring.

## Build

```bash
gradle --no-daemon assembleDebug
```

Run no-emulator unit tests:

```bash
gradle --no-daemon :app:testDebugUnitTest
```
