# Privacy Policy

Last updated: June 26, 2026

This Privacy Policy applies to Zzzzzz Sleep Trigger, package name `com.zzzzzz.sleeptrigger`.

Zzzzzz Sleep Trigger is an Android phone and Wear OS app that detects sleep and wake-related activity so it can run user-approved local automation tasks, such as pausing media playback after sleep is detected and recording the time of the event.

## Developer and Contact

Developer: Zzzzzz Sleep Trigger developer

Privacy inquiries: use the developer contact email shown on the Zzzzzz Sleep Trigger Google Play listing.

## Data the App Accesses or Stores

Zzzzzz Sleep Trigger may access or store the following data, depending on the features and permissions the user enables:

- Sleep and wake event times.
- Trigger history, scheduled task times, task execution times, task results, and error details.
- Android activity-recognition signals used to infer sleep or post-wake movement.
- Health Connect sleep-session records, when the user grants Health Connect sleep read access.
- Wear OS Health Services activity states and watch sensor telemetry, such as activity state, movement, heart-rate availability, and on-body/off-body status, when enabled on the watch.
- Active media-session state needed to pause currently playing media.
- Local permission and feature status used to show whether required app permissions are enabled.

The app is designed to avoid storing song titles, notification text, account credentials, contacts, location, photos, messages, call logs, payment information, advertising identifiers, or precise device identifiers.

## How the App Uses Data

Zzzzzz Sleep Trigger uses accessed data only for app functionality:

- To detect when the user appears to be asleep.
- To detect when the user stands up or becomes active after waking.
- To schedule and run the user-selected automation task after the configured delay.
- To pause active media playback on the phone.
- To show local event history and diagnostic status to the user.
- To import recent completed sleep sessions from Health Connect for sleep-history reconciliation.
- To send sleep or wake trigger messages from the Wear OS companion app to the paired phone app.

The app does not use this data for advertising, profiling, sale, or unrelated analytics.

## Data Sharing

Zzzzzz Sleep Trigger does not sell personal or sensitive user data.

The current app does not include its own cloud service, analytics SDK, advertising SDK, or general internet permission. Sleep, activity, watch telemetry, media-control state, and event history are processed locally on the user's devices.

The Wear OS companion may send sleep or wake trigger messages to the paired phone through Google Play services Wear OS device communication. Health Connect access is handled through Android's Health Connect permission system. Media pause behavior uses Android media-session and notification-listener APIs on the user's device.

Data may be disclosed if required by applicable law or a valid legal process.

## Permissions

The app may request these permissions or special accesses:

- Activity recognition, to detect sleep-related or post-wake movement.
- Health Connect sleep read access, to import completed sleep sessions.
- Notification permission, to show app and foreground-service status.
- Notification listener access, to find and pause active media sessions.
- Wear OS body sensor and activity-recognition permissions, to support watch-side sleep and wake detection.
- Foreground service permission on Wear OS, to keep watch telemetry active when the user enables that feature.
- Boot completed permission, to restore enabled monitoring after device restart.

Permission-gated features do not work until the user grants the required permission or special access.

## Retention and Deletion

Event history and status data are stored locally on the user's device. The app does not create an account and does not maintain a remote user profile.

Users can delete local app data by using Android system settings for the app, uninstalling the app, or using an in-app delete-history control when available. Uninstalling the app removes app-managed local data from the device, subject to normal Android backup and device behavior.

Future versions should keep a clear in-app event-history delete action for sleep-trigger history. Until that control is available, users can delete local history through Android app storage settings or by uninstalling the app.

## Security

Personal and sensitive data is kept local by default. Data stored by the app uses Android app-private storage. Watch-to-phone trigger delivery is scoped to the paired devices and app communication path. The phone app also declares a signature-level permission for local trigger broadcasts so unrelated apps cannot inject trigger events through that broadcast path.

Users should keep their phone, watch, Android system, Google Play services, Health Connect, and Zzzzzz Sleep Trigger app updated.

## Children

Zzzzzz Sleep Trigger is intended for adults and is not directed to children.

## Changes to This Policy

This policy may be updated as the app changes. Material changes should be reflected in this document and in the app's Google Play Data safety disclosures before release.
