# Performance budgets

Documented as part of TASK-175 (Phase 12). These are the numbers the app must hit on the reference **low-end** device (5" 720p, quad-core A55, 3 GB RAM, minSdk API 24). Better devices should exceed them comfortably.

## Budgets

| # | Metric | Budget | Rationale |
|---|---|---|---|
| P1 | Cold start → first reader frame | **≤ 1500 ms** | The charter promise is "open the app and read." Anything beyond ~1.5 s breaks the illusion. |
| P2 | Warm start → first reader frame | **≤ 500 ms** | Warm start is the common case after brief background time. |
| P3 | Page-turn latency (finger lift → committed page) | **≤ 100 ms** | Perceived instantaneous. Text measurement + font shaping must not block the frame. |
| P4 | Typical search query end-to-end | **≤ 200 ms** | FTS5 skeleton index is small (~700 KB); this is achievable. |
| P5 | Steady-state reader memory (heap + native) | **≤ 120 MB** | Fits low-end 3 GB devices without pressure. |
| P6 | Install size (APK, universal) | **≤ 12 MB** | Bundled DB ≈ 3.2 MB, two fonts ≈ 434 KB, Compose + Hilt runtime; comfortable headroom. |
| P7 | Dropped frames during steady swipe | **≤ 2 per page-turn (avg)** | Occasional dropped frame is tolerated; sustained jank is not. |
| P8 | ANR count in a 30-min soak | **0** | Reader ANRs are release-blocking. |

## Verification methods (device-required)

- P1, P2 — Android Studio Profiler cold-start trace, or `am start -W` on adb.
- P3, P7 — GPU rendering profile bars + FrameTiming API on the reference device; Perfetto trace for anomalies.
- P4 — In-app timing wrapping `QuranRepository.search()` (already runs on `Dispatchers.IO`).
- P5 — `dumpsys meminfo app.mushaf` after 5-minute steady reading session.
- P6 — Play Console upload result; `bundletool` locally.
- P8 — Manual + soak scripts under `/qa/soak/` (Phase 14 output).

## Current status

All measurement is deferred to device verification. The app is compile-untested on a device until the first CI-built APK is installed. First round of measurement lives in `qa/perf-baseline.md` (to be written after first phone install).

## Optimizations already in place

- Bundled read-only Quran DB (no first-run import time).
- Small in-memory Surah + Juz caches in `QuranRepositoryImpl`.
- 5-page LRU cache + ±1 preload in `ReaderViewModel`.
- Debounced (500 ms) reading-position saves — no DataStore hammering on rapid swipes.
- Debounced (300 ms) search-query pipeline — no FTS spam.
- All DAO calls run on `Dispatchers.IO` via `@IoDispatcher`.
- R8 + resource shrinking enabled for release builds (see `android/app/build.gradle.kts`).

## Optimizations reserved for after first measurement

- Baseline Profiles once we have a real profileable APK on device.
- FTS query result cache (last N queries) — only if perf measurement shows FTS is hot.
- Bitmap decode optimization — not applicable yet (no images beyond system icons).
