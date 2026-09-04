# ADR-0023: Phone-first build & test workflow — cloud CI builds APKs, side-load to user's device

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** whenever the developer's local machine changes, or before Phase 3

## Context and problem statement

The developer's PC is not suitable for running Android Studio, the full Android SDK, or emulators (developer stated: "PC is a little slow"). Development still needs to iterate on real Android code and see it running on a phone. This constraint materially affects the build system and the testing strategy — deserving of an ADR alongside ADR-0018 and ADR-0019.

## Decision drivers

- Local footprint must be small; no Android Studio, no emulator, no heavy IDE.
- User has an Android phone available for testing.
- Iteration must remain fast enough that developing screens is productive.
- Reproducible builds — CI is the source of truth for release artifacts.

## Options considered

### Option A — Full local Android Studio + SDK + emulator

Rejected per developer constraint.

### Option B — Local `JDK 17 + Android SDK command-line tools + platform-tools (adb) + Gradle wrapper`, cloud CI for release builds

Local: JDK 17 (~200 MB) + `cmdline-tools` + `platform-tools` (adb, ~50 MB) + Gradle wrapper (project-local, no global install). ~500 MB local footprint. Debug builds `./gradlew :app:installDebug` install to phone via `adb`. Release builds happen in CI.

Downside: `~/.gradle/caches/` grows to ~2 GB over time with Kotlin / Android dependencies. First build downloads ~500 MB of dependencies. Kotlin compile time is CPU-bound.

### Option C — Cloud-only. No local Android build capacity. Push to a `dev` branch → CI builds → download APK → install on phone (recommended, with C+B fallback)

Local: **only** JDK 17 (~200 MB) + adb (~50 MB). No Gradle cache. Iteration loop: edit → git push → wait 2–4 minutes for CI → download APK → adb install. Slow but zero local build burden.

### Option D — Cross-platform (React Native / Flutter)

Off-topic for this ADR (would supersede ADR-0001/0002).

## Decision

**Primary: Option C (cloud-only builds).**
**Fallback: Option B (local builds when needed for tight iteration).**

Concrete workflow:

1. **Local footprint (required):**
   - JDK 17 (Temurin)
   - `platform-tools` (adb only)
   - The Gradle wrapper checked into the repo (no global Gradle install)
   - `.gradle/` cache directory allowed but not required
2. **Iteration loop (default):**
   - Edit code in any editor (VS Code recommended for lightness).
   - Push to a branch (e.g., `dev` or a feature branch).
   - **GitHub Actions** builds `app-debug.apk` and uploads it as a workflow artifact.
   - Download the artifact, `adb install -r app-debug.apk` to the phone.
3. **Optional local build (Option B fallback):**
   - When the CI round-trip is too slow (rapid UI iteration), run `./gradlew :app:installDebug` locally. Requires the SDK command-line tools + a build-tools + platform installed via `sdkmanager`.
4. **Testing:**
   - Domain / data / view-model tests (ADR-0018) run in the same CI, gating merges.
   - Real-device smoke test: `./gradlew connectedAndroidTest` when a device is plugged in, or manual APK install + walk-through for the smoke case.
5. **Release builds** (Phase 17+): CI-only, signed with the upload key from GitHub Actions secrets (or equivalent). See ADR-0020.

Note: the developer has stated they do not want to upload to GitHub for CI purposes yet. The repo is private on GitHub; CI is provisioned when the developer opts in. **Until CI is set up, the effective workflow is:** (a) do documentation / planning tasks locally with no build, or (b) delay Android code work until CI is opted-in. This choice defers TASK-030 (Android project init) until the build strategy is operational.

## Consequences

- Positive: minimal local footprint; reproducible builds in CI; the developer's PC stays cool.
- Negative: iteration latency (2–4 min CI round-trip) is real; mitigated by the Option B fallback for tight UI work; also by making Composable previews (Robolectric-based screenshot tests in CI) part of the loop so we validate look/feel without an APK download.
- Follow-up:
  - When developer is ready to enable CI: add `.github/workflows/build-debug.yml`, `build-release.yml`, `test.yml`.
  - TASK-030 (Android project init) is blocked on this operational decision.

## References

- ADR-0018 (testing), ADR-0019 (build system), ADR-0020 (release strategy).
- GitHub Actions Android: https://docs.github.com/en/actions/publishing-packages/publishing-and-installing-a-package-with-github-actions
