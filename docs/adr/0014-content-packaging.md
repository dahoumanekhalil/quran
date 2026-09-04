# ADR-0014: Ship Quran content as a bundled read-only SQLite asset

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 4

## Context and problem statement

Given ADR-0013 (fully offline for MVP), how do we package the Quran dataset in the app?

## Decision drivers

- Immutability at runtime — the DB must not be writable by the app.
- Cold-start performance — reader open time budget on low-end devices is tight.
- Update path — a future content v1.0.1 must be shippable via a standard app update.
- No first-run parsing step (JSON import at first launch would delay Time-to-First-Read).

## Options considered

### Option A — Prebuilt SQLite in `assets/` + copy to internal storage on first launch, opened read-only (recommended)

The pipeline in Phase 1 already produces `quran-1.0.0.db`. On first launch, verify SHA-256 against a bundled constant, copy to `context.filesDir/quran.db`, then open with `SupportSQLiteOpenHelper.Configuration.builder().openHelperFactory(...)` in read-only mode. Subsequent launches skip the copy.

### Option B — Raw JSON in `assets/`, parse on first launch into Room

Rejected: 6236 inserts at first launch adds seconds of delay; parsing bloats install with intermediates we don't need at runtime.

### Option C — Custom binary format

Rejected: reinvents SQLite (queries, indices, FTS5) with no offsetting benefit.

## Decision

**Option A.**

Bundled file: `android/app/src/main/assets/quran/quran-1.0.0.db`.
Bundled hash constant: `android/app/src/main/assets/quran/quran-1.0.0.db.sha256` OR compiled into a Kotlin constant generated at build time from the pipeline manifest.

First-launch flow:

1. If `filesDir/quran.db` exists AND its recorded install-hash matches the bundled hash → skip.
2. Else copy `assets/quran/quran-1.0.0.db` → `filesDir/quran.db`, verify SHA-256 against bundled constant, record the install-hash.
3. Open the file via a plain `SupportSQLiteOpenHelper` in read-only mode.
4. FTS5 queries go through the bundled `ayahs_fts` virtual table (built in Phase 1 TASK-023).

Version bumps: content v1.0.1 replaces the bundled file and the bundled hash constant; the copy step detects the mismatch and re-copies.

## Consequences

- Positive: instant first-launch reading; integrity verified at every copy; no schema migrations needed since the DB is read-only.
- Negative: two copies of the DB briefly exist on disk during the first-launch copy — acceptable.
- Follow-up: TASK-055 (Phase 4) implements the copy-and-open helper.

## References

- ADR-0007, ADR-0013.
- Bundled DB in Android: https://developer.android.com/training/data-storage/room/prepopulate
