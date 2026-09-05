# Reading position — edge-case matrix

Documented as part of TASK-133. The reading-position pipeline is:

```
Reader swipe → ReaderViewModel.onPageChanged(N)
  → SavedStateHandle["reader_current_page"] = N            (survives rotation)
  → pageChanges.tryEmit(N) → 500ms debounce → save         (survives process death)
     → ReadingPositionRepositoryImpl.save(...)
     → UserPreferencesStore.saveReadingPosition(...)
     → DataStore prefs["last_page"] = N
```

On restore:

```
Reader open → SavedStateHandle["reader_current_page"] ?: readingPositionRepository.get().pageNumber ?: 1
```

## Scenarios

| # | Scenario | Expected | Covered by |
|---|---|---|---|
| 1 | Fresh install | Reader opens on page 1 | UserPreferencesStore fallback `prefs[KEY_LAST_PAGE] ?: 1`; `ReadingPosition.DEFAULT` |
| 2 | Normal foreground exit + reopen | Resumes at last read page | `ReaderViewModelTest.restoresFromRepositoryWhenSavedStateEmpty` |
| 3 | Configuration change (rotation) | Same page after rotation | `SavedStateHandle["reader_current_page"]` restore path; `ReaderViewModelTest.restoresFromSavedStateWhenPresent` |
| 4 | Force stop via system settings | Resumes at last read page | Debounced save + DataStore persistence; **device-verified only** |
| 5 | Battery kill / OOM | Resumes at last read page | Same as #4; **device-verified only** |
| 6 | Split-screen entry | Reader stays on current page | Compose lifecycle handles; **device-verified only** |
| 7 | Backup/restore reset (clear app data) | Page 1 (data cleared) | DataStore reset → falls back to default per #1 |
| 8 | Dataset version bump, same page semantics | Same page | Dataset SHA-256 gate in `QuranDb`; page numbers 1..604 are stable across schema-compatible bumps |
| 9 | Dataset schema bump, incompatible page semantics | Fallback to page 1, log event | Not yet exercised — v1.0.0 is the only dataset. Fallback logic to be added when v2 lands. |

## What still requires a device

Scenarios 4, 5, 6 need real Android hardware:

- **Force stop:** `adb shell am force-stop app.mushaf` → relaunch → verify page.
- **Battery kill / OOM:** exercise with `adb shell am kill app.mushaf` (needs debuggable build) or via long background time on a low-memory device.
- **Split-screen:** long-press recents → open in split → verify no state loss.

These are tracked as manual verification on the QA pass in Phase 14. The code paths they exercise (SavedStateHandle + debounced DataStore save + repository read) are already exercised by unit tests in JVM environments.

## Dataset-mismatch fallback (scenario 9)

When we ship dataset v2, add a `datasetVersion` field to the saved position and compare on load:

```kotlin
if (saved.datasetVersion != CURRENT_DATASET_VERSION) {
    // log or telemetry event
    ReadingPosition.DEFAULT   // opens on page 1
}
```

For MVP (single-dataset v1.0.0) this is a no-op.
