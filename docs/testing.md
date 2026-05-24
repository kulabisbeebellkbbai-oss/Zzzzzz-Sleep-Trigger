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

- `gradle --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` passes with the Wear OS Data Layer dependency.
- The phone app exposes a 10-second simulated sleep trigger for local verification and a 5-minute simulated sleep trigger matching the first requested delay scenario.
- The phone app exposes a manual stood-up-after-wake trigger route.
- `testmedia/` provides a local MediaSession target for verifying that Zzzzzz can pause an active media session through notification listener access.
- Waydroid verification passed for the Wear UI stood-up path: tapping the Wear app's stood-up control delivered `STOOD_UP_AFTER_WAKE` to the phone receiver, executed `PAUSE_MEDIA`, and the test media target recorded `pausedByController=true`.
- Waydroid verification passed for the Wear UI asleep path: tapping the Wear app's asleep control delivered `ASLEEP_DETECTED`, recorded a scheduled `PAUSE_MEDIA` task with the default 5-minute delay, and left media playing until the scheduled time.
- Physical target identified: TicWatch Pro 3 Ultra GPS paired with a Ulefone Armor X16 Pro.
- Ulefone Armor X16 Pro wireless ADB connected on 2026-05-24 at `192.168.68.102:39233`; phone APK installed and launched on Android 15.
- Ulefone notification listener access was granted by shell and verified live in `dumpsys notification`.
- Ulefone physical media-pause verification passed for the immediate stood-up route: the phone app's `Trigger` button recorded `STOOD_UP_AFTER_WAKE`, executed `PAUSE_MEDIA`, and `testmedia/` recorded `pausedByController=true`.
- Ulefone physical media-pause verification passed for the delayed sleep route: the phone app's `Run 10 sec` button scheduled `ASLEEP_DETECTED`, AlarmManager fired the delayed task, and `testmedia/` recorded `pausedByController=true`.
- TicWatch direct install/testing is still blocked until watch ADB wireless debugging IP and port are available.

Current runtime limitation:

- The Android SDK emulator still depends on host virtualization being enabled in firmware for normal performance. Waydroid is the active local runtime for now.
- The Wear APK now uses the same application ID as the phone APK so Wear OS Data Layer private app messages can route between the paired devices. This is correct for separate phone/watch devices, but it means the phone and Wear APKs cannot both be installed into one single Android runtime under the default debug variant.

## Physical Device Setup

For the Ulefone phone:

1. Enable Developer options.
2. Enable USB debugging or Wireless debugging.
3. Connect it to this host and accept the RSA debugging prompt.
4. Verify with `adb devices -l`.

For the TicWatch:

1. Enable Developer options.
2. Enable ADB debugging and Debug over Wi-Fi.
3. Connect with `adb connect <watch-ip>:5555` if direct watch install is needed.
4. Install `wear/build/outputs/apk/debug/wear-debug.apk` to the watch and `app/build/outputs/apk/debug/app-debug.apk` to the phone.

After install, grant notification listener access on the phone for Zzzzzz, start media playback on the phone, then tap `Stood up` in the Wear app to verify immediate media pause through the Data Layer path.
