# Permission audit

TASK-190. This app declares **zero permissions** in the Android manifest.

## Current state (`android/app/src/main/AndroidManifest.xml`)

No positive `<uses-permission>` elements. The manifest additionally uses `tools:node="remove"` directives for `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, and `com.google.android.gms.permission.AD_ID`. These are defensive **denials** — they guarantee that a future transitively-added permission from a dependency AAR cannot silently reach the merged manifest without a code review catching the diff. None of our current dependencies request these; the removes are future-proofing, not fixing an active leak.

Verified 2026-09-06.

## Rationale — feature-by-feature

| Feature | Would-normally-need | We use instead |
|---|---|---|
| Read Quran text + pagination | (nothing — bundled asset) | Bundled read-only SQLite in `assets/quran/quran.db`, copied to `context.filesDir` on first launch |
| Store reading position, settings, bookmarks | (nothing — internal storage) | `context.filesDir` DataStore. Internal-only, no user permission needed. |
| Full-text search | (nothing — bundled index) | FTS5 virtual table already inside `quran.db` |
| Keep screen on while reading | `WAKE_LOCK` — **not needed** | `FLAG_KEEP_SCREEN_ON` via Window flag (per-Activity, no permission required) |
| Font rendering | (nothing) | Both fonts bundled as `res/font/*` assets |
| Update / integrity check | `INTERNET` — **not requested** | SHA-256 of the bundled DB is baked into `QuranAsset.CONTENT_SHA256` at build time. Verified on first-launch copy. |

## What we intentionally avoid

- `INTERNET` / `ACCESS_NETWORK_STATE` — the charter mandates fully-offline operation. Even opt-in telemetry is out of scope for MVP.
- `WAKE_LOCK` — window-flag approach is sufficient and permission-free.
- `POST_NOTIFICATIONS` — no notifications.
- `FOREGROUND_SERVICE` — no services.
- `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`, etc. — no background scheduling.
- Storage permissions (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`) — everything lives in scoped internal storage.

## `allowBackup="false"`

Manifest sets `android:allowBackup="false"`. Reasoning: reading position and bookmarks are per-device by design; the roadmap treats cross-device sync as an explicit user-initiated export/import (POST-MVP), not as automatic cloud backup. Users who reinstall get a fresh start at page 1 — consistent with the charter's local-first philosophy.

## Verification method

Manual manifest review. Any new dependency that transitively adds a `<uses-permission>` must be scrutinized and either justified in this file or rejected. Dependency review lives in `docs/dependencies.md`.

To see the **merged** manifest (which reflects transitive contributions from every AAR), open the compiled manifest at `android/app/build/intermediates/merged_manifests/debug/AndroidManifest.xml` after a build. Grep for `uses-permission`. Anything besides the four `tools:node="remove"` entries is a red flag.

## Google Play Data Safety implications

- Data collected: **none**.
- Data shared: **none**.
- Data processed on-device only: reading position, bookmarks, settings. Not linked to identity, not sold, not shared.
- Full declaration: `docs/privacy-policy.md`.
