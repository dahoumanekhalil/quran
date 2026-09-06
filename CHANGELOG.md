# Changelog

All notable changes to the Mushaf Android app.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The `versionCode` shown in each release is derived from `versionName` per the formula documented in `docs/release-cadence.md` — `major * 10000 + minor * 100 + patch`.

## [Unreleased]

Working towards the first Play Store internal-testing release.

### Added
- Core Reader with Madani 604-page mushaf pagination and horizontal RTL swipe (`:feature:reader`).
- Full offline search across the Quran with skeleton-normalized FTS5 index (`:feature:search`).
- Page-scoped bookmarks with a ribbon toggle on the reader chrome (`:feature:bookmarks` via `:feature:navigation`'s `Saved` tab).
- Navigation via Surah / Juz / direct page-jump + a `Saved` tab for bookmarks (`:feature:navigation`).
- Settings screen with theme (Light / Dark / Follow system), text size (Small / Medium / Large), Mushaf font (KFGQPC / Amiri Quran), and keep-screen-on toggle (`:feature:settings`).
- About screen with app version, dataset SHA-256, credits, and a `mailto:` feedback link.
- Reading-context strip in the reader top chrome (Arabic surah name + Juz N + Page N/604).
- Tap-to-toggle chrome + immersive edge-to-edge reading mode.
- Reduced-motion respect (`Settings.Global.TRANSITION_ANIMATION_SCALE`).
- Full accessibility semantics — `contentDescription` on every icon, list-row `onClickLabel`, auto-mirrored icons for RTL locales. Reader ayah text uses `clearAndSetSemantics { }` so TalkBack does not TTS-read Arabic verses (charter safeguard).
- Error states for page-load failure and search failure — quiet, non-alarming, recoverable.

### Fixed (during the pre-release audit cycle)
- **Main-thread I/O on cold start.** `QuranDb` was doing a 3.2 MB copy + SHA-256 hash in its constructor. Now deferred to a suspend `database()` accessor guarded by a Mutex, first call runs on IO.
- **White-flash on dark theme cold start.** `Theme.Mushaf` was forcing `Theme.Material.Light.NoActionBar`. Changed to `android:Theme.DeviceDefault.NoActionBar` so the initial window follows OS dark mode.
- **Invalid stored reading page could crash the pager.** `UserPreferencesStore.readingPosition` now clamps to page 1 on read if the stored value is out of range.
- **Out-of-range bookmarks displayed if the JSON was tampered with.** `parseBookmarks` now filters to `1..604` on load.
- **Infinite-spinner on page-load failure.** `PageRenderer` now uses an explicit `PageLoadState` sealed interface — errors surface as "Could not load page N — <reason>".
- **Search silently returned no-results when the search actually failed.** `SearchViewModel` now distinguishes error from empty and surfaces via `SearchUiState.error` + dedicated `ErrorState` composable.

### Security / privacy
- Zero positive `<uses-permission>` in the manifest.
- Defensive `tools:node="remove"` for `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `com.google.android.gms.permission.AD_ID` so a future transitive dependency cannot silently add them.
- `NOTICE` file attributes every runtime code library (all Apache 2.0) alongside content + font licenses.
- No third-party analytics / advertising / crash-reporting SDKs — Play Console vitals is the only observability channel.

### Notes
- CI (`.github/workflows/app-build.yml`) now runs `:app:bundleRelease` on every push so R8 + resource-shrinking + kotlinx-serialization keep-rule regressions surface immediately. The unsigned AAB + R8 mapping are uploaded as artifacts.
- `versionCode` is derived from `versionName` — bumping one keeps the other in sync.

## Template for future releases

```
## [x.y.z] — YYYY-MM-DD

### Added
- (features that shipped)

### Changed
- (behavior changes; call out anything user-visible)

### Fixed
- (specific bug fixes)

### Security / privacy
- (any privacy/security change that affects the audit claims — must also update
  docs/permissions.md, docs/dependencies.md, docs/privacy-policy.md)

### Content
- (any dataset or content-corpus change — must reference the CONTENT_MANIFEST
  entry and re-run the Phase 1 gates, per docs/content-corrections.md)
```
