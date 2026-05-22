# Privacy and Security

## Data Sensitivity

Sleep times, wake times, sensor-derived activity, and media app state are sensitive. The app should collect only what is required to execute user-approved tasks.

## Initial Data Policy

- Store event history locally on the phone.
- Do not upload sleep, sensor, or media-session data by default.
- Do not store song titles or notification text unless a future feature explicitly requires it.
- Store package names for media pause diagnostics only when useful to the user.
- Provide a clear event-history delete option.

## Permission Principles

- Request permissions only when the user enables a feature that needs them.
- Explain notification access before sending the user to Android's special app access screen.
- Treat Health Connect as user-owned data and request only sleep permissions required for this app.
- Keep watch sensor processing minimal and task-focused.

## Public Repository Rules

- Do not commit signing keys, keystores, local SDK paths, private notes, generated APKs, or local agent metadata.
- Keep `.agents/` and `.codex/` local-only unless a future file is intentionally sanitized for publication.

