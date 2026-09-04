# ADR-0018: Testing — JUnit 5 + Turbine for unit/domain; Compose UI test + Robolectric where appropriate

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 3

## Context and problem statement

The app needs a test strategy that catches regressions in the reader engine, data pipeline, and user-critical flows (bookmark, position restore, search), while being runnable in the phone-first, low-local-tooling environment (see ADR-0023).

## Decision drivers

- Charter #11 — extreme stability; the reader cannot crash.
- Domain and data layers should be testable without an Android device.
- UI critical paths (page navigation, bookmark save/restore, search) need coverage.
- Tests must run in the cloud CI (ADR-0023) as gating checks.

## Options considered

### Option A — JUnit 5 + Kotest assertions + Turbine (Flow testing) — for pure JVM tests

Fastest test lifecycle, no Android runtime. Covers domain services, repository mappers, view models (with `runTest`), pipeline logic on the Python side.

### Option B — Robolectric — for tests that need Android APIs but not a device

Runs "Android" tests on the JVM by shadowing Android classes. Useful for testing DataStore, Room migrations, some Context-dependent code. Slower than pure JVM, faster than instrumented.

### Option C — Compose UI test (`androidx.compose.ui:ui-test-junit4`) — for Compose components

Runs on a device or via Robolectric (Compose UI test supports Robolectric mode since Compose 1.5). Screen-level flow tests: swipe, navigate, bookmark.

### Option D — Instrumented tests on device (Espresso / `androidx.test`)

Runs on the phone directly (matches ADR-0023). Slowest. Reserve for a small "smoke" suite that verifies real-device rendering.

## Decision

**Combined strategy:**

- **Domain, data, and view-model tests → JUnit 5 + Turbine.** JVM-only, fast, exhaustive.
- **Room migrations, DataStore, Context-flavored code → Robolectric.** Small suite.
- **Compose screen tests → Compose UI test in Robolectric mode.** Preview-driven for regression on `ScreenContent(state = ...)` stateless composables (leverages ADR-0012).
- **Real-device smoke → 1 test that boots the app, opens the reader, swipes 10 pages, saves a bookmark, restarts, verifies restoration.** Runs on the user's phone via `adb`.

Pipeline (Python) uses `pytest` with the existing validation stages doubling as tests.

## Consequences

- Positive: 95%+ of tests run in the cloud in seconds; regressions caught before APK download; real-device suite is small and fast enough to run manually per PR.
- Negative: Compose UI test in Robolectric mode has known edge cases (some animation-dependent behavior) — accept, and augment with device smoke if a regression slips.
- Follow-up: TASK-030 wires the test toolchain into Gradle.

## References

- Turbine: https://github.com/cashapp/turbine
- Robolectric: https://robolectric.org/
- Compose UI test: https://developer.android.com/develop/ui/compose/testing
- ADR-0023 (cloud CI).
