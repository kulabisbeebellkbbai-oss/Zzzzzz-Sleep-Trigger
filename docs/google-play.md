# Google Play Publishing

Package name: `com.zzzzzz.sleeptrigger`
Application id: `com.zzzzzz.sleeptrigger`
Play Console app state: API checked 2026-06-26; package not found for the configured service account
Primary track: internal

## Signing

Upload key path: `~/.local/share/codex/android-upload-keys/com.zzzzzz.sleeptrigger.jks`
Signing properties: `~/.local/share/codex/android-upload-keys/com.zzzzzz.sleeptrigger.properties`
Upload certificate SHA-256: `3B:4A:6C:B0:3B:2A:A6:36:6E:68:A8:38:5B:2A:6B:B4:60:AA:7C:91:22:02:96:DE:11:58:19:0A:CA:C7:CB:81`
Play App Signing configured: owner verification required in Play Console

## Store Listing

App name: Zzzzzz Sleep Trigger
Short description: Pause phone media automatically when sleep is detected.
Full description: Zzzzzz Sleep Trigger pairs an Android phone app with a Wear OS companion to run user-approved sleep and wake automations. The current task pauses active media playback after sleep is detected and records the event locally. Sleep detection uses phone activity recognition and watch telemetry, with local event history and an overnight routing window to reduce daytime false positives.
Category: Health & Fitness
Contact email: owner required
Privacy policy URL: owner required

Assets:

- App icon: required
- Feature graphic: required before broad release
- Phone screenshots: required
- Wear screenshots: recommended

## Compliance

Data safety notes: Sleep, activity, and media-control state are processed locally by default. The app requests activity recognition, Health Connect sleep read access, notification listener access for media control, notification permission, and Wear OS body sensor/activity permissions for telemetry.
Content rating notes: Sleep and automation utility; no user-generated public content.
Target audience: adults
Ads declaration: no ads
Login required for review: no
Reviewer credentials location:

## Testing

Internal tester group: `codex-internal-testers`
Closed tester group: `codex-closed-testers`
License testers required: no

## Release

Version code: 1
Version name: 0.1.0
Phone bundle path: `app/build/outputs/bundle/release/app-release.aab`
Wear bundle path: `wear/build/outputs/bundle/release/wear-release.aab`
Release notes: Initial internal test build for sleep-triggered media pause and wake/stand-up automation validation.

## Owner-Only Play Console Tasks

- Create the first app shell for `com.zzzzzz.sleeptrigger` if it does not already exist. The Android Publisher API returned `Package not found: com.zzzzzz.sleeptrigger` on 2026-06-26.
- Enable Play App Signing and register the local upload certificate.
- Add the service account JSON at `~/.config/codex/google-play/service-account.json` and grant app-scoped access.
- Complete privacy policy, data safety, content rating, target audience, contact details, and store-listing assets.
- Confirm whether phone and Wear artifacts should be published as one package/release or separate Play Console artifacts before non-internal rollout.
