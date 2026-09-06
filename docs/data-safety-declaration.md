# Google Play Data Safety declaration — TASK-267

Backing evidence: `docs/permissions.md`, `docs/dependencies.md`, `docs/privacy-policy.md`. Every claim below is grep-verifiable in the source.

## Section: Data collection and sharing

### Does your app collect or share any of the required user data types?

**No.**

### Is all of the user data collected by your app encrypted in transit?

**N/A** — no data is transmitted.

### Do you provide a way for users to request their data be deleted?

**N/A** — no data is collected. Uninstalling the app removes the on-device reading position, bookmarks, and settings (there is no cloud copy to delete).

## Data type coverage

Every category asked by Play Console — answered NO.

| Category | Collected? | Shared? |
|---|---|---|
| Personal info (name, email, user IDs, address, phone, race/ethnicity, political/religious affiliation, sexual orientation, other) | No | No |
| Financial info (all subcategories) | No | No |
| Health & fitness | No | No |
| Messages | No | No |
| Photos and videos | No | No |
| Audio files | No | No |
| Files and docs | No | No |
| Calendar | No | No |
| Contacts | No | No |
| App activity (interactions, search history, installed apps, other) | No | No |
| Web browsing | No | No |
| App info and performance (crash logs, diagnostics, performance) | No | No |
| Device or other IDs (Android Advertising ID, device ID, etc.) | No | No |
| Location (approximate, precise) | No | No |

## Security practices

### Is your app committed to follow the Google Play Families Policy?

Not applicable — we are not opting into Google Play's Designed for Families program. The app is family-safe by construction (no ads, no communication features, no sensitive content beyond religious text — see the content rating declaration).

### Does your app support account deletion?

**N/A** — no accounts exist.

## Independent audit questions

If reviewed by Google or an auditor:

- **Where is user data stored?** Locally, in the app's private storage (`context.filesDir` + DataStore). Cleared on uninstall.
- **Is Android auto-backup enabled?** No. `android:allowBackup="false"` is set intentionally.
- **Does the app use any Google Play Services APIs?** No — none imported (verify via `docs/dependencies.md`).
- **Does the app use any tracking / analytics / advertising SDKs?** No — audited in `docs/dependencies.md`.
- **Does the app declare the INTERNET permission?** No — verify with `grep uses-permission android/app/src/main/AndroidManifest.xml`.

## Content rating declaration (TASK-268 inputs)

Selected answers for the Play content rating questionnaire:

- App type: Reference / Religious text.
- Not a game.
- No user-generated content.
- No communication features (no chat, no comments).
- No advertising.
- No location.
- No purchases.
- No violence, adult content, or gambling.

Expected rating: **Everyone** (US ESRB) / **PEGI 3**.

## Target audience

Adults and children of all ages. The Quran itself is the content; there is nothing else in the app.

## Health, financial, insurance app declaration

**None applicable.**

## Compliance summary

- ADR-0013 (offline strategy) ← binding
- ADR-0023 (build/test workflow) ← binding
- Product Principles §1–§7 in `docs/CHARTER.md` ← binding

Any future change that would introduce data collection MUST update this file first, and must be re-approved against the charter (which currently makes such changes impossible without a superseding principle).
