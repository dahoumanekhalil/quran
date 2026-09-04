# ADR-0007: Three-tier persistence — bundled SQLite + Room + DataStore

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 4

## Context and problem statement

The app has three qualitatively different classes of persistent state:

1. **Immutable religious content** — the Quran text + pagination + juz/hizb/rub/sajda. Frozen at `data/output/RELEASE/quran-1.0.0.db` (see Phase 1).
2. **Mutable user data** — bookmarks, last-read position, per-ayah notes (POST-MVP).
3. **Lightweight settings** — theme, keep-screen-on, font size, chosen font when we ship alternates.

Mixing these three concerns into one store risks compromising Quran-content integrity (charter #8).

## Decision drivers

- Charter #7 (offline for all core Quran content) and #8 (content is immutable religious material).
- Simplicity — three well-fitted tools beat one poorly-fitted tool.
- Testability of each layer independently.
- Startup performance (asset copy at first launch must be fast).

## Options considered

### Option A — Everything in a single Room database

Simple but Room migrations touch the Quran tables every schema change, risking accidental content mutation. Also awkward to ship a large read-only content asset alongside a writable Room DB.

### Option B — Three-tier (recommended)

- Immutable Quran content: **prebuilt SQLite asset**, bundled in `assets/quran/quran-1.0.0.db`, copied to internal storage on first launch, opened **read-only** thereafter. Integrity verified against a bundled SHA-256.
- Mutable user data: **Room** database (`user.db`) — bookmarks, reading position, notes.
- Lightweight settings: **DataStore Preferences** — theme, keep-screen-on, font size.

### Option C — All files (custom binary + JSON files)

Rejected: reinvents transactions, indices, and integrity checks that SQLite / Room / DataStore already give us.

## Decision

**Option B — three-tier persistence.**

The Quran content DB is opened via `SupportSQLiteOpenHelper` in read-only mode (or Room's `createFromAsset(...)` with a Room facade *only if* schema migration is never needed for that DB — but our DB is fully immutable, so a plain `SupportSQLiteDatabase` is simpler).

User data uses Room with a `Migration` chain from schema v1 onward.

Settings use DataStore Preferences (typed access via `Preferences.Key` extensions).

## Consequences

- Positive: Quran content cannot be accidentally mutated by user-data schema changes. Bundled DB avoids first-launch import time (~10s for 6236 ayah insertions is avoided). DataStore is simpler than SharedPreferences and coroutine-friendly.
- Negative: three storage APIs to learn — mitigated by the layering being obvious (Quran vs bookmarks vs settings).
- Follow-up: ADR-0014 (content packaging) covers the asset packaging + copy strategy in detail. TASK-055 will implement the Android data source in Phase 4.

## References

- Room: https://developer.android.com/training/data-storage/room
- DataStore: https://developer.android.com/topic/libraries/architecture/datastore
- Bundled SQLite asset guidance: https://developer.android.com/training/data-storage/room/prepopulate
