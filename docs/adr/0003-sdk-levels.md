# ADR-0003: minSdk 24, targetSdk 35, compileSdk 35

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** at each Google Play targetSdk requirement bump; also before Phase 18

## Context and problem statement

Android apps declare three SDK levels: `minSdk` (lowest supported OS), `targetSdk` (behavioral compatibility target — Google Play mandates a floor), and `compileSdk` (SDK version to compile against). The reader must run on low-end phones (charter #10) while staying eligible for Google Play distribution.

## Decision drivers

- Reach: percentage of active devices supported.
- Play Store eligibility (Google raises the `targetSdk` floor annually).
- Access to modern APIs (Vector Drawables Support, dynamic colors, splash screen API, edge-to-edge, etc.).
- Jetpack Compose baseline (`androidx.activity:activity-compose` requires minSdk 21+).
- Kotlin coroutines runtime footprint on old devices.

## Options considered

### Option A — minSdk 21, target/compile 35

Reaches ~99.9% of active devices. Adds compatibility shims for 21–23 that we do not need (runtime permissions were only 6.0+, notification channels 8.0+, etc.). More `if (Build.VERSION.SDK_INT >= ...)` in code.

### Option B — minSdk 24, target/compile 35 (recommended)

Reaches ~99% of active devices (Android 7.0+, released 2016). Simpler code (Java 8 language APIs available; no permission-model split). Aligns with typical modern Android baselines.

### Option C — minSdk 26 or higher

Cleaner (no Multidex needs, full Java 8+ Time API, etc.), but drops meaningful device share and violates charter #10 (low-end devices matter).

## Decision

**`minSdk = 24`, `targetSdk = 35`, `compileSdk = 35`.**

The `targetSdk` value MUST match the current Google Play requirement at release time. As of 2026 the floor is 34; we set 35 to stay ahead. Revisit annually.

## Consequences

- Positive: ~99% device coverage; access to modern APIs without a compatibility burden; single permission model (runtime).
- Negative: users on Android 6.0 (Marshmallow) and below cannot install the app — acceptable given remaining device share.
- Follow-up: Phase 3 Gradle configuration (TASK-030) sets these values. Phase 18 rechecks the current Play `targetSdk` floor and updates this ADR.

## References

- Google Play `targetSdk` policy: https://support.google.com/googleplay/android-developer/answer/11926878
- Android version distribution: Android Studio "Help me choose" dialog and https://apilevels.com/
