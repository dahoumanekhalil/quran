# Device matrix checklist — TASK-235

Runs on your phone. Reproduce this checklist per device in the matrix and archive results in `qa/device-matrix.md` when done.

Given ADR-0023 (phone-first workflow), MVP relaxes the roadmap's 4-device matrix to **1 primary device + documented gaps**. If a second device is available, run it too.

## Devices to run

| Slot | Class | Example | Rationale |
|---|---|---|---|
| Primary | Your phone | (whatever you carry daily) | Baseline for real use |
| Optional 1 | Low-end / 3-4 year-old budget | any spare device | Catches perf regressions |
| Optional 2 | Modern flagship | if borrow-able | Catches OEM quirks |
| Optional 3 | Tablet | POST-MVP; smoke only | Confirms no catastrophe on large screens |

## Per-device QA script

Copy the block below and mark each line PASS/FAIL/NOTE.

```
Device: __________________________
OS version: ______________________
APK build: mushaf-<run-number>-debug.apk
Date: ____________________________

Launch
  [ ] Fresh install → app opens to page 1 in the Reader
  [ ] Returning user → app opens to last-read page (verify by installing an update)
  [ ] Cold start feels fast (subjectively < 2s)

Reader — page-turn behavior
  [ ] Swipe left → advances to page 2 (RTL — visual left = next page)
  [ ] Swipe right → returns to page 1
  [ ] Swipe through 20 pages continuously → no visible jank
  [ ] Land on pages 1, 2, 3, 50, 300, 604 — text renders correctly
  [ ] Font (KFGQPC) shapes correctly, no tofu boxes, no missing glyphs

Reader — chrome
  [ ] Tap the center of a page → chrome hides + system bars go immersive
  [ ] Tap again → chrome + bars come back
  [ ] Chrome shows Surah name (Arabic), Juz N, Page N/604 — updates as you swipe
  [ ] Back button hides chrome when visible; then falls through to system nav

Navigation — via burger (☰)
  [ ] Surahs tab lists all 114 surahs; tap Al-Fatihah → lands on page 1
  [ ] Juz tab lists 30 juz; tap Juz 2 → lands on the correct page
  [ ] Page tab accepts 1..604; invalid values are quietly rejected
  [ ] Saved tab lists bookmarks (may be empty at first)
  [ ] Gear icon (top-right of Navigation) → Settings screen

Search — via search icon in top-right of reader chrome
  [ ] Type "بسم" → hint disappears, results appear (< 2 sec)
  [ ] Tap a result → lands on that ayah's page in the Reader
  [ ] Clear query (X icon) → hint returns
  [ ] Query with fewer than 2 (normalized) chars → no results, hint stays

Bookmarks
  [ ] Reader chrome ribbon icon toggles between outlined ↔ filled when tapped
  [ ] Swipe to another page → ribbon updates
  [ ] Saved tab shows all bookmarks with page + saved date
  [ ] Tap a bookmark → lands on that page
  [ ] Delete icon on a bookmark row → removes it from the list

Settings — via ☰ menu → gear icon
  [ ] Theme = Dark → whole app darkens
  [ ] Theme = Follow system → matches OS theme
  [ ] Text size = Large → Reader text visibly bigger
  [ ] Font = Amiri Quran → Reader renders with the OFL font instead of KFGQPC
  [ ] Keep screen on = off → screen dims per system settings during reading

About — via Settings → "About Mushaf"
  [ ] Version number appears
  [ ] SHA-256 matches: 0ba78b6ab99f57a5688adc572f35ccf0568716a48d4424378d73cfd91c0b093e
  [ ] Back arrow returns to Settings

State restoration
  [ ] Navigate to page 200 → force-close from recent apps → relaunch → lands on page 200 (± 1 within debounce window)
  [ ] Rotate device on the reader → state preserved
  [ ] Enable split-screen → reader still readable

Offline verification
  [ ] Enable airplane mode
  [ ] Cold launch → all features still work: reader, search, bookmarks, settings

Regression check — audit-cycle fixes
These verify the specific bugs closed during the Phase 12/13/15 audits are
actually fixed on device. If any of these fails, the audit claim was wrong
and needs a re-open in PROJECT_TASKS.md.

  [ ] Set Theme = Dark in Settings → close the app → cold launch → NO white flash
      before the reader renders. (Phase 13 fix: Theme.Mushaf → DeviceDefault.)
  [ ] Cold-launch on the reference device feels immediate, not laggy for the
      first ~200 ms. (Phase 12 fix: QuranDb DB copy + hash moved off main thread.)
  [ ] If you can force a page load failure (e.g. clear app data mid-read, then
      quickly try to swipe — race is narrow, best-effort), the reader shows
      "Could not load page N" instead of an infinite spinner. (Phase 15 fix:
      PageRenderer PageLoadState.Error branch.)
  [ ] Turn on Developer Options → Transition animation scale = Off → open the
      reader → tap center → chrome hides/shows INSTANTLY (no fade). Turn scale
      back on → chrome fades in/out. (Phase 11 fix: reduced-motion respect.)
  [ ] Switch system language to Arabic → open About → back arrow visually
      points RIGHT (was left in English). Open Settings → the "About Mushaf"
      row chevron points LEFT (was right). (Phase 11 fix: AutoMirrored icons.)
  [ ] Try to install the CI-built debug APK alongside another Mushaf install
      (if you have one). They must coexist. Debug shows as "Mushaf (debug)" or
      similar. (Phase 17 fix: applicationIdSuffix = ".debug".)

Notes / defects (log below)
- 
```

## What to do with results

If all boxes pass on your phone → mark TASK-235 Completed with the log attached and move to Phase 17.

If any critical box fails → report the specific line and I'll fix. Do not proceed to Phase 17 with an outstanding fail.
