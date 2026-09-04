# PROGRESS.md — Digital Mushaf, Phase 0 → Phase 6

Snapshot of everything shipped in the repository so far, in chronological build order. This document is a **status/handoff** — not a roadmap. The roadmap is [`PROJECT_TASKS.md`](PROJECT_TASKS.md); the vision is [`docs/CHARTER.md`](docs/CHARTER.md).

Frozen date of this snapshot: **2026-09-04**

---

## Top-line summary

| Phase | Status | Deliverable |
|---|---|---|
| Phase 0 — Project Preparation | ✅ Complete | Charter + 24 ADRs + repo tooling |
| Phase 1 — Quran Data Foundation | ✅ Complete + frozen | `data/output/RELEASE/quran-1.0.0.db` |
| Phase 2 — Typography & Rendering | ✅ Structural | Font selection (KFGQPC + Amiri Quran) + browser preview + Android prototype |
| Phase 3 — Android Foundation | ✅ Core | 8-module Gradle project under `android/` |
| Phase 4 — Core Data & Domain | ✅ Essentials | Quran/reading-position/settings repos; bookmarks deferred to Phase 9 |
| Phase 5 — Reader Engine | ✅ Structural | Reader ViewModel + HorizontalPager + preloading + debounced position save |
| Phase 6 — Quran Navigation | ✅ Complete | Surah list, Juz list, direct page-jump, "return to reading" wired via repository |
| Phase 7 — Home Experience | ⏳ Next | (roadmap suggests "open directly to Quran" home; we already land on the reader) |

**Physical footprint of the repo:**
- Frozen SQLite Quran DB: **3.2 MB**
- Two Mushaf fonts: KFGQPC (297 KB) + Amiri Quran (137 KB)
- Vendored `quran-center/quran-meta` (TS sources for provenance): ~200 KB
- Tanzil source XMLs (with provenance): ~4 MB
- Total repo: ~15 MB

**Anticipated APK size** (once built): ~5–6 MB debug (DB + fonts + Compose + Hilt).

---

## Phase 0 — Project Preparation

### Charter & governance

- [`docs/CHARTER.md`](docs/CHARTER.md) — vision, 16 non-negotiable principles ("no ads / no subs / no accounts / no tracking / no telemetry / fully offline / Quran integrity paramount"), MVP / POST-MVP / FUTURE scope, explicit non-goals.
- [`README.md`](README.md), [`CONTRIBUTING.md`](CONTRIBUTING.md), [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md), [`LICENSE`](LICENSE) (MIT for project code), [`NOTICE`](NOTICE) + [`LICENSES/`](LICENSES/) (Tanzil CC BY 3.0, quran-meta MIT, SIL OFL, KFGQPC EULA).
- `.editorconfig`, `.gitattributes` — LF everywhere, vendored dirs untouched.

### ADRs (24 total, all Accepted)

Every one lives in [`docs/adr/`](docs/adr/). Template + index at [`docs/adr/README.md`](docs/adr/README.md).

| # | Decision |
|---|---|
| 0001 | Kotlin as primary language |
| 0002 | Jetpack Compose as UI toolkit (with `AndroidView(TextView)` fallback pre-approved) |
| 0003 | minSdk 24 / targetSdk 35 / compileSdk 35 |
| 0004 | Clean-layered MVVM with unidirectional data flow |
| 0005 | Navigation Compose |
| 0006 | `ViewModel` + `StateFlow` + immutable `UiState` + `SavedStateHandle` |
| 0007 | Three-tier persistence: bundled read-only SQLite (Quran) + Room (user data) + DataStore (settings) |
| 0008 | Hilt for DI (KSP) |
| 0009 | Kotlin Coroutines + Flow |
| 0010 | Data layer: repositories over data sources, mappers to domain types |
| 0011 | Domain layer: thin services (**not** one class per use case) |
| 0012 | UI layer: stateless `ScreenContent(state, onEvent)` + hoisted `UiState` |
| 0013 | Fully offline for MVP core Quran content (no `INTERNET` perm) |
| 0014 | Quran shipped as bundled read-only SQLite asset in `assets/quran/` |
| 0015 | Bundled OTF Mushaf font, single primary in MVP |
| 0016 | Compose `HorizontalPager` for the Mushaf page-turn |
| 0017 | Compose `BasicText` for ayah rendering (TextView fallback) |
| 0018 | Testing: JUnit 5 + Turbine + Robolectric + Compose UI test |
| 0019 | Gradle Kotlin DSL + version catalog + multi-module topology |
| 0020 | Play App Signing + SemVer + staged rollout |
| 0021 | Tanzil Uthmani v1.1 (CC BY 3.0) as text source |
| 0022 | `quran-center/quran-meta` Madani 15-line 604-page pagination |
| **0023** | **Phone-first build/test workflow — cloud CI + APK side-load** (constraint-driven, added as extra ADR) |
| 0024 | Primary Mushaf font — KFGQPC Uthmanic Hafs v2.2; Amiri Quran alternate |

### Source-of-truth data model

[`docs/data-model.md`](docs/data-model.md) — storage-independent Quran schema and invariants (114 surahs / 6236 ayahs / 604 pages / 30 juz / 60 hizb / 240 rub / 7 manzil / 556 ruku / 15 sajda).

---

## Phase 1 — Quran Data Foundation

### Sources (all archived with SHA-256 + `SOURCE.json` manifests)

- `data/sources/tanzil-uthmani/1.1/` — Tanzil Project Uthmani XML + TXT + Uthmani-min variants. **CC BY 3.0**, verbatim only.
- `data/sources/quran-meta/` — `quran-center/quran-meta` @ commit `a5dd4a4`, package `6.1.1-6`. **MIT**.
- `data/sources/quran-com/v4/` — Quran.com API v4 (KFGQPC-derived text) used for cross-source verification. **Independent lineage.**
- `data/sources/fonts/kfgqpc-uthman-taha/` — KFGQPC Uthmanic Hafs v2.2 TTF (bundled in the app).
- `data/sources/fonts/amiri-quran/` — Amiri Quran v1.003 TTF (bundled alternate).

### Deterministic pipeline

[`data/pipeline/pipeline.py`](data/pipeline/pipeline.py) — one Python script, 9 stages, all outputs byte-reproducible across runs:

```
parse_tanzil → parse_meta → merge
   → validate_structural → validate_unicode → cross_source_diff
   → package_sqlite → build_search_index → release
```

- **Validation:** 114 surahs, 6236 ayahs, 604 pages, 30 juz, 60 hizb, 240 rub, 7 manzil, 556 ruku, 15 sajda — all invariants pass.
- **Unicode profile** ([`data/output/intermediate/unicode-profile.md`](data/output/intermediate/unicode-profile.md)): 710,229 chars, 70 distinct codepoints, all in allowed Arabic Unicode blocks.
- **Cross-source verification** vs Quran.com KFGQPC: 6236/6236 verses skeleton-equal; 6126/6236 byte-identical; 110 diacritic-only differences (documented, expected).
- **Search index:** FTS5 over `search_text` column (loose alef-stripped normalization — see `search-normalization.md` memory). 9/9 canonical queries pass.

### Frozen release

- **File:** `data/output/RELEASE/quran-1.0.0.db` (3,260,416 B)
- **SHA-256:** `0ba78b6ab99f57a5688adc572f35ccf0568716a48d4424378d73cfd91c0b093e`
- **Content manifest:** `data/output/RELEASE/CONTENT_MANIFEST.md`

This DB is the single source of truth consumed by the Android app.

---

## Phase 2 — Typography & Rendering Prototype

### Font selection (TASK-025)

Both candidates verified via `fontTools.getBestCmap()` to cover **all 70 codepoints** in the frozen corpus:

- **Primary:** KFGQPC Uthmanic Hafs v2.2 — the typeface that prints the physical Madani mushaf. License permits redistribution unmodified in a free app. Ships in the APK.
- **Alternate:** Amiri Quran v1.003 — SIL OFL 1.1 (safest license); ships alongside as a documented fallback.

License text in [`LICENSES/KFGQPC-EULA.txt`](LICENSES/KFGQPC-EULA.txt) and [`LICENSES/SIL-OFL-1.1.txt`](LICENSES/SIL-OFL-1.1.txt). ADR: [`docs/adr/0024-font.md`](docs/adr/0024-font.md).

### Browser preview (self-contained)

`prototype/font-preview/index.html` — 600 KB self-contained HTML with both fonts embedded as base64 data URIs. Renders pages 1, 2, 3, 50, 300, 600, 604 with live font / size / line-height toggles. Regenerated by `python prototype/font-preview/build.py` from the frozen corpus.

Purpose: gets us ~80% of the visual-side-by-side check from TASK-026 **without** needing any Android tooling on the PC.

### Throwaway Android rendering prototype (`prototype/rendering/`)

Minimal Kotlin + Compose app with `HorizontalPager` over 604 pages, DB + both fonts in assets, in-app toggles. Fully throwaway — deleted before v1.0.

### GitHub Actions CI

`.github/workflows/prototype-build.yml` — Ubuntu, JDK 17, Android SDK 35, Gradle 8.10.2. Uploads `app-debug.apk` as artifact on push touching `prototype/rendering/**` or manual dispatch.

### What's left in Phase 2 (needs device verification)

- **TASK-027, TASK-028, TASK-029** (device matrix + long-session + rendering-lock ADR) — need the user to run the built APK on the phone. Roadmap's 3-device matrix is unrealistic; will relax to "1 phone + documented gaps" once first run is complete.

---

## Phase 3 — Android Foundation (`android/`)

### Module topology (per ADR-0019)

```
android/
├── settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/libs.versions.toml
├── app/                              assembly, DI graph, Application, MainActivity, NavHost
├── core/
│   ├── common/                       dispatcher qualifiers + Hilt DI for them
│   ├── designsystem/                 Mushaf theme, palette, typography, fonts (res/font/)
│   ├── database/                     QuranDb + QuranDao (bundled SQLite asset)
│   ├── datastore/                    DataStore for reading position + settings
│   ├── data/                         Repository implementations (Hilt @Binds)
│   └── domain/                       Pure Kotlin entities + repository interfaces
└── feature/
    ├── reader/                       ReaderScreen + ReaderViewModel + preloading + debounced save
    └── navigation/                   Surah list, Juz list, page-jump
```

Not yet created (deferred to their phases): `feature/bookmarks` (Phase 9), `feature/search` (Phase 8), `feature/settings` (Phase 10).

### Versions

Kotlin 2.0.21, Compose BOM 2024.10.01, AGP 8.7.2, Hilt 2.52, Room 2.6.1 (dep pinned but unused yet), DataStore 1.1.1, Navigation Compose 2.8.4, Coroutines 1.9.0.

### App CI

`.github/workflows/app-build.yml` — builds `android/:app:assembleDebug` on any push touching `android/**`, uploads APK + test reports as artifacts.

---

## Phase 4 — Core Data & Domain

### Domain layer (`:core:domain`)

Pure Kotlin, no Android imports. Entities: `Surah`, `Ayah`, `Page`, `PageAyah`, `Juz`, `ReadingPosition`, `AppSettings` (+ enums `RevelationPlace`, `ThemeMode`, `MushafFont`). Repository interfaces: `QuranRepository`, `ReadingPositionRepository`, `SettingsRepository`.

### Database layer (`:core:database`)

- `QuranAsset` — bundled DB coordinates (path + version + expected SHA-256).
- `QuranDb` — copies the bundled asset to internal storage on first launch, **verifies SHA-256**, opens read-only. Any hash mismatch throws — satisfies TASK-055 (content self-check on start).
- `QuranDao` — coroutine-based SQL accessors on `Dispatchers.IO`. Returns domain types.
- `di/DatabaseModule` — Hilt Singleton for `QuranDb`.

### DataStore layer (`:core:datastore`)

- `UserPreferencesStore` — one DataStore file (`user_prefs`), typed Flows for reading position + settings. Debounced writes happen in the Reader ViewModel, not here.
- `di/DataStoreModule` — Hilt Singleton.

### Data layer (`:core:data`)

- `QuranRepositoryImpl` — in-memory caches `allSurahs()` and `allJuz()` (hit on almost every screen).
- `ReadingPositionRepositoryImpl` and `SettingsRepositoryImpl` — thin wrappers over `UserPreferencesStore`.
- `di/DataModule` — Hilt `@Binds` for the three repositories.

Bookmarks (TASK-053) intentionally deferred to Phase 9.

Domain use-case classes (TASK-054) intentionally **not** created — per ADR-0011, ViewModels call repositories directly and thin services are added only when coordination is genuinely needed.

---

## Phase 5 — Reader Engine (`:feature:reader`)

- `ReaderUiState` — immutable data class (per ADR-0012). `ReaderEvent` sealed interface.
- `ReaderViewModel`:
  - Restores initial page from `SavedStateHandle` (config change) or `ReadingPositionRepository` (process death / cold start).
  - Observes `SettingsRepository` for font / size / line height updates.
  - Observes `ReadingPositionRepository.observe()` to react to jumps written by other screens (NavigationScreen); loop-safe via a `lastPersistedPage` guard.
  - Debounced (500 ms) position save on swipe.
  - Small LRU page cache (`5`) + preload of ±1 page around the current page.
- `ReaderScreen`:
  - Compose `HorizontalPager` inside `CompositionLocalProvider(LocalLayoutDirection provides Rtl)` so swipe direction follows Arabic reading order.
  - `PageRenderer` builds an `AnnotatedString` per page, with surah headers, inline bismillah (except surah 9 and inline for surah 1), Arabic-numeral ayah markers, KFGQPC font, justified text.
  - Minimal top chrome — burger button (→ Navigation) + "Page N / 604" label.

Not yet: font switching UI, gesture control-visibility, immersive mode with system-bar hide/show (TASK-074, TASK-075).

---

## Phase 6 — Quran Navigation (`:feature:navigation`)

- `NavigationViewModel` — loads all surahs + juz on init, exposes `jumpToSurah`, `jumpToJuz`, `jumpToPage`. Each writes the target page to `ReadingPositionRepository` and invokes an `andThen` callback (typically `nav.popBackStack()`).
- `NavigationScreen` — three tabs (Surahs / Juz / Page). Surah list uses `MushafArabicFont` for Arabic names + Latin transliteration + English translation subtitle. Juz list shows starting surah/ayah/page. Page jump is a numeric text field + confirm button with `1..604` validation.
- Cross-screen jump semantics: Navigation writes reading position → pops back → Reader observes the position change → syncs pager. Clean and testable (no `SavedStateHandle` string-typing).

---

## App wiring (`:app`)

- `MushafApp` — `@HiltAndroidApp` Application.
- `MainActivity` — `@AndroidEntryPoint`, `enableEdgeToEdge()`, hosts `MushafTheme { MushafNavHost() }`.
- `MushafNavHost` — two destinations: `reader` (start) and `navigation`. Simple, no nav arguments.

---

## What's NOT in this snapshot

- Bookmarks feature module (Phase 9).
- Search feature module (Phase 8) — DB has FTS5 ready, the UI does not.
- Settings screen (Phase 10) — `SettingsRepository` and DataStore exist; no UI to change them yet.
- About screen (Phase 10).
- Static analysis (ktlint/detekt) and layer-boundary lint (TASK-034).
- Test suites (deps wired; tests not written yet — TASK-035).
- App icon (uses Android's default `sym_def_app_icon`).
- Light/dark theme switching via UI (theme respects system, no override yet).
- Font switching UI (Amiri Quran is bundled but only selectable via DataStore, no screen).
- Immersive full-screen reading mode (TASK-075).
- Baseline profile / R8 / release build hardening.

---

## How to actually run this

1. Push the repository to GitHub (already done — `git@github.com:dahoumanekhalil/quran.git`).
2. GitHub Actions runs `.github/workflows/app-build.yml` on any push to `main` touching `android/**`.
3. Open the Actions tab, wait for the run, download the `mushaf-app-apk` artifact.
4. Copy the APK to your phone.
5. Enable "Install from unknown sources" for your file manager once, then tap the APK to install.
6. Launch **Mushaf** — you'll land on the reader at page 1 (or your last-read page on subsequent launches). Swipe left = next page (RTL). Tap the burger menu → jump by Surah / Juz / Page number.

Same path works for `prototype-build.yml` if you want the throwaway rendering prototype.

---

## Known risks / things the author has not verified

Because the developer's PC intentionally has no Android tooling (per ADR-0023), **nothing in `android/` has been compiled or run on a device by this author**. The first CI run is the gate:

- **Compile errors** are possible despite careful writing — surfaces on the first CI run, fixable by iteration.
- **Runtime shaping** of Uthmani text is the highest technical risk; validated in Phase 2 via the browser preview but **must be re-verified on real Android** during TASK-027–029.
- **Hilt / KSP** version pairing (Kotlin 2.0.21 ↔ KSP 2.0.21-1.0.28 ↔ Hilt 2.52) — chosen for compatibility but only proven by CI.
- **Gradle configuration cache** is enabled in `gradle.properties`; if any plugin doesn't support it, disable there.
- **NavHost routes** use string constants — safe for MVP; migrate to type-safe destinations (Kotlin serialization) if the destination graph grows.
- **RTL swipe direction** is enforced via `CompositionLocalProvider(LocalLayoutDirection provides Rtl)` around the pager — validated in the prototype's design, must confirm on device.

If anything fails, the CI log is the authoritative feedback surface. Each failed run is a quick iteration cycle: read log → edit → push → re-run.

---

## Next phase (Phase 7 — Home Experience)

Roadmap tasks TASK-100..TASK-101. The intent is a minimal home surface — but per charter, the app should "immediately return to the Quran at their last reading position" (already the current behavior since the Reader IS the start destination). Phase 7 may end up being a small polish pass rather than a new screen.
