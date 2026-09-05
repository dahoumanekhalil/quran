# ADR-0025: Bookmark scope — page-scoped, DataStore-JSON storage

- **Status:** Accepted
- **Date:** 2026-09-05
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 15 (visual refinement) and again if POST-MVP notes ship

## Context and problem statement

TASK-053 deferred bookmark storage to this ADR. Two orthogonal decisions must be locked before writing code:

1. **Scope of a bookmark** — page-level ("this page is worth returning to") vs. ayah-level ("this specific verse").
2. **Storage medium** — Room (per ADR-0007) vs. DataStore-serialized JSON.

## Decision drivers

- Charter: reading experience > everything else. Bookmarks must feel like a Mushaf ribbon, not a tag manager.
- Interaction model already anchored on pages (`HorizontalPager`, 604-page mushaf pagination). Ayah highlighting is deferred (TASK-116 snippet + POST-MVP inline highlight).
- Forward compatibility: POST-MVP roadmap explicitly calls out "ayah-level bookmarks with notes." The MVP data model must be a strict subset of that so no migration is needed later.
- Simplicity — every dependency, module, and schema we add is a maintenance surface.

## Options considered

### Scope — Option A: page-scoped only

One row per bookmarked page. Add / remove is idempotent per page. UI is a filled/outlined ribbon icon on the reader chrome.

**Pros:** Trivial UI. Matches the physical Mushaf ribbon metaphor. Interaction has zero learning curve.

**Cons:** Can't mark a specific verse in a long surah.

### Scope — Option B: ayah-scoped only

One row per bookmarked ayah. Reader chrome shows bookmarked ayah highlight.

**Pros:** More expressive.

**Cons:** Requires ayah selection UX (tap-to-highlight) which is not built and would add gesture load to the reader — competing with TASK-074's tap-to-toggle chrome. Overkill for MVP.

### Scope — Option C: both, distinguished by `ayah_global_index IS NULL`

Same row shape as A, but the schema carries a nullable `ayah_global_index` so a future POST-MVP release can start writing ayah-scoped rows without a migration.

**Pros:** Locks forward compatibility with zero MVP cost.

**Cons:** None material.

### Storage — Option A: Room per ADR-0007

Add a `bookmarks` table to a new user Room database. Full SQL queries.

**Pros:** Aligned with ADR-0007. Supports queries like "bookmarks in juz 3" if we ever need them.

**Cons:** New module, KSP round-trip, migration chain to maintain. Overkill for a list of ≤100 records with no query needs.

### Storage — Option B: DataStore with kotlinx.serialization JSON string

Store the full bookmarks list as a JSON blob in the existing `user_prefs` DataStore. Reads/writes go through a typed `BookmarksRepository`.

**Pros:** Zero new module surface. No Room/KSP setup. Matches how reading-position and settings already work in `:core:datastore`. Fast for realistic list sizes (typical user: 5–20 bookmarks; hard ceiling ~100).

**Cons:** Every read/write serializes the whole list. Deviates from ADR-0007 — but ADR-0007 didn't anticipate that the MVP feature would need no SQL queries.

## Decision

**Scope: Option C** — page-scoped bookmarks in MVP with a nullable `ayahGlobalIndex` field. All MVP writes leave `ayahGlobalIndex = null`. Ayah-scoped bookmarks in POST-MVP will populate the same field with no schema change.

**Storage: Option B** — DataStore + kotlinx.serialization JSON. Explicit deviation from ADR-0007, scoped to bookmarks only. Reading position and settings continue to use their existing typed `Preferences.Key`s. If POST-MVP notes / queries force it, migration to Room is a one-file swap of `BookmarksRepositoryImpl` — the interface holds.

## Data model

```kotlin
data class Bookmark(
    val pageNumber: Int,                      // 1..604
    val ayahGlobalIndex: Int? = null,         // POST-MVP; null in MVP
    val createdAtEpochMillis: Long,
)
```

Uniqueness key: `(pageNumber, ayahGlobalIndex)`. Adding an already-present pair is a no-op. Default sort: `createdAtEpochMillis DESC`.

## API

```kotlin
interface BookmarksRepository {
    fun observe(): Flow<List<Bookmark>>            // sorted desc by createdAt
    suspend fun add(pageNumber: Int)               // no-op if page already bookmarked
    suspend fun remove(pageNumber: Int)            // matches on pageNumber only in MVP
    suspend fun isBookmarked(pageNumber: Int): Boolean
    fun observeIsBookmarked(pageNumber: Int): Flow<Boolean>
}
```

## Consequences

- Positive: Fast to implement, tiny APK impact, zero new module. Forward-compatible with POST-MVP ayah bookmarks + notes. Reader chrome affordance is a single toggle icon — matches charter's minimal-controls principle.
- Negative: JSON serialization on every mutation. Acceptable at expected list sizes; will not be a bottleneck.
- Follow-up: If POST-MVP introduces bookmark notes >100 chars each or query-heavy features (bookmark search, filter by juz, etc.), migrate to Room. Migration path: read the JSON list once, insert into a fresh `bookmarks` Room table, delete the JSON key.

## References

- Roadmap TASK-130 through TASK-132 (Phase 9).
- ADR-0007 (three-tier persistence) — deviated from for MVP bookmarks only.
- POST-MVP scope in `docs/CHARTER.md` — "Ayah-level bookmarks with notes."
