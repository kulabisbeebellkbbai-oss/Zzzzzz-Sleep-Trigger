# Testing Plan

## Unit Tests

- Trigger definition validation.
- Delay calculation.
- Event log persistence.
- Task result state transitions.
- Stand-up debounce rules.

## Integration Tests

- Simulated sleep event schedules a delayed task.
- Delayed task pauses mocked media controllers.
- Failed media control records a task error.
- Wake event plus movement fires the second trigger once.
- Wake event plus minor movement does not fire the stand-up trigger.

## Device Tests

- Phone app onboarding and permission state.
- Notification listener grant flow.
- Active media session pause against common media apps.
- Wear OS companion permission flow.
- Watch-to-phone trigger delivery.
- Health Connect sleep-session import after a completed sleep session.

## Toolchain Verification

Use the local Android toolchain:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/29.0.14206865"
export GRADLE_HOME="$HOME/.local/opt/gradle-9.5.1"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$GRADLE_HOME/bin:$PATH"

gradle --no-daemon assembleDebug
```

Current verification:

- `gradle --no-daemon assembleDebug` passes for `app` and `wear`.
- The phone app exposes a 10-second simulated sleep trigger for local verification and a 5-minute simulated sleep trigger matching the first requested delay scenario.
