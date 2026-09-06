# Runtime dependency register

TASK-191. Every runtime library the app ships with — purpose, license, and privacy posture.

Last audited: **2026-09-06**. Source of truth: `android/gradle/libs.versions.toml`.

## Rule

**No dependency that transitively pulls in advertising, analytics, telemetry, or tracking is permitted.** Any new dependency must justify its inclusion here before being added.

## Runtime dependencies

| Group | Artifact | Version | Purpose | License | Network? |
|---|---|---|---|---|---|
| org.jetbrains.kotlin | kotlin-stdlib | 2.0.21 | Kotlin runtime | Apache 2.0 | No |
| org.jetbrains.kotlinx | kotlinx-coroutines-android | 1.9.0 | Concurrency | Apache 2.0 | No |
| org.jetbrains.kotlinx | kotlinx-serialization-json | 1.7.3 | Bookmarks JSON round-trip | Apache 2.0 | No |
| androidx.core | core-ktx | 1.13.1 | AndroidX core extensions | Apache 2.0 | No |
| androidx.lifecycle | lifecycle-runtime-ktx | 2.8.7 | Lifecycle observation | Apache 2.0 | No |
| androidx.lifecycle | lifecycle-viewmodel-compose | 2.8.7 | ViewModel + Compose | Apache 2.0 | No |
| androidx.lifecycle | lifecycle-runtime-compose | 2.8.7 | collectAsStateWithLifecycle | Apache 2.0 | No |
| androidx.activity | activity-compose | 1.9.3 | ComponentActivity + setContent | Apache 2.0 | No |
| androidx.compose (BOM) | compose-bom | 2024.10.01 | UI toolkit | Apache 2.0 | No |
| androidx.compose.foundation | foundation | (BOM) | HorizontalPager, LazyColumn | Apache 2.0 | No |
| androidx.compose.material3 | material3 | (BOM) | Material 3 primitives | Apache 2.0 | No |
| androidx.compose.material | material-icons-extended | (BOM) | Icons (Menu, Search, Bookmark, etc.) | Apache 2.0 | No |
| androidx.compose.ui | ui, ui-graphics, ui-tooling-preview | (BOM) | Compose core | Apache 2.0 | No |
| androidx.navigation | navigation-compose | 2.8.4 | Route graph | Apache 2.0 | No |

**Debug-only runtime** (`debugImplementation` — ships in debug APK, stripped from release):

| Group | Artifact | Version | Purpose | License | Network? |
|---|---|---|---|---|---|
| androidx.compose.ui | ui-tooling | (BOM) | Layout inspector + `@Preview` support in Android Studio | Apache 2.0 | No |

**Runtime — continued:**
| androidx.datastore | datastore-preferences | 1.1.1 | Reading position + settings + bookmarks | Apache 2.0 | No |
| androidx.sqlite | sqlite-framework | 2.4.0 | Direct SQLite access for the bundled DB | Apache 2.0 | No |
| androidx.sqlite | sqlite-ktx | 2.4.0 | Kotlin sugar for above | Apache 2.0 | No |
| com.google.dagger | hilt-android | 2.52 | DI | Apache 2.0 | No |
| androidx.hilt | hilt-navigation-compose | 1.2.0 | Hilt + hiltViewModel() | Apache 2.0 | No |

Room (`androidx.room:room-*`) is declared in the version catalog but currently unused (deferred; ADR-0025 uses DataStore instead of Room for bookmarks).

## Test-only (do not ship)

| Group | Artifact | Version | Purpose |
|---|---|---|---|
| org.junit.jupiter | junit-jupiter-api/engine/params | 5.11.3 | Unit tests |
| app.cash.turbine | turbine | 1.2.0 | Flow assertions |
| com.google.truth | truth | 1.4.4 | Assertions |
| io.mockk | mockk | 1.13.13 | Mocking |
| org.jetbrains.kotlinx | kotlinx-coroutines-test | 1.9.0 | Test dispatcher |

## Build-only (do not ship)

Android Gradle Plugin 8.7.2, Kotlin/KSP compilers, Hilt compiler, Spotless + ktlint 1.3.1, `de.mannodermaus.android-junit5` 1.11.2.0.

## Forbidden

No entry in the runtime table above may be replaced or supplemented by:

- Any Google Play Services library (`com.google.android.gms:*`) unless explicitly justified. MVP has none.
- Any Firebase library (`com.google.firebase:*`). MVP has none.
- Any analytics SDK (`com.mixpanel.*`, `com.amplitude.*`, `com.segment.*`, `io.branch.*`, etc.).
- Any advertising SDK (`com.google.android.gms:play-services-ads`, `com.facebook.audience.*`, etc.).
- Any crash reporter that phones home by default without explicit opt-in and no data collected on-device (Crashlytics, Bugsnag, Sentry-with-network).

## How to verify

```sh
cd android
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
```

Grep for the forbidden group IDs. If any appear (even transitively), reject the change that pulled them in.
