# Digital Mushaf — Android app

Production Android app. Multi-module Gradle + Kotlin + Jetpack Compose per ADRs 0001, 0002, 0019.

## Module topology

```
:app                              assembly, DI graph, Application, Activity, NavGraph
:core:common                      dispatchers, utilities
:core:designsystem                theme, colors, typography, Mushaf fonts (in res/font/)
:core:database                    Quran SQLite accessor (from bundled asset), Room for user data
:core:datastore                   DataStore for reading position + settings
:core:data                        Repository implementations
:core:domain                      Domain entities, repository interfaces (pure Kotlin/JVM)
:feature:reader                   Mushaf reader screen (HorizontalPager over 604 pages)
:feature:navigation               Surah list, Juz list, direct page jump
```

Skipped for now (later phases per PROJECT_TASKS.md):
- `:feature:bookmarks` — Phase 9
- `:feature:search` — Phase 8
- `:feature:settings` — Phase 10

## Building

Per ADR-0023 (phone-first workflow) the primary build is **cloud CI**:

- Any push to `main` touching `android/**` triggers `.github/workflows/app-build.yml`, which uploads `app-debug.apk` as an artifact.
- Download the APK, install to your phone: file-manager side-load OR `adb install -r app-debug.apk`.

**Local build** requires JDK 17 + Android SDK cmdline tools with:
- `platforms;android-35`
- `build-tools;35.0.0`
- `platform-tools`

Then either use system Gradle (`gradle assembleDebug`) or generate the wrapper first (`gradle wrapper --gradle-version 8.10.2`; then `./gradlew assembleDebug`).

## Package structure

Root package: `app.mushaf`
Application ID: `app.mushaf`
minSdk 24, targetSdk / compileSdk 35 (ADR-0003).

## Testing

- Domain / data / view-model tests: JUnit 5 + Turbine (ADR-0018). Run via `gradle :core:domain:test :core:data:test`.
- Compose UI + integration: added later per Phase 3 TASK-035.

## What's implemented in this pass (Phase 3–6)

- End-to-end reading path: launch → land on last-read page → swipe through 604 pages.
- Navigation: surah list, juz list, direct page jump. All wired via Navigation Compose.
- KFGQPC Uthmanic Hafs v2.2 as primary font (ADR-0024).
- Content self-check on first launch: verifies bundled `quran.db` SHA-256.
- Reading position persisted (DataStore, debounced).
- Configuration-change survival via ViewModel + SavedStateHandle.

## What's NOT implemented yet

- Bookmarks (Phase 9).
- Search (Phase 8).
- Settings screen (Phase 10) — theme is hardcoded light for now; DataStore exists but no UI to change.
- About screen (Phase 10).
- Accessibility polish (Phase 11).
- Process-death resilience for pager scroll offset (only page number persists).
- Font switching (Amiri Quran shipped but not exposed).
