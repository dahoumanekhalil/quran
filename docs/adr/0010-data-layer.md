# ADR-0010: Data layer — repositories over data sources, mappers to domain types

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 5

## Context and problem statement

The data layer must hide storage details (bundled SQLite, Room, DataStore) from the domain and UI, expose Kotlin-friendly domain types, and remain testable.

## Decision drivers

- ADR-0004 architectural rules (UI → domain → data, no upward dependencies).
- Testability — repository interfaces mockable, data sources fake-able.
- Immutable Quran content vs mutable user data (see ADR-0007).

## Options considered

### Option A — Screens call Room DAOs directly

Simple, fast, but blows through architecture (charter #14 / ADR-0004). Rejected.

### Option B — Repository per aggregate; data sources hidden behind (recommended)

Repositories expose domain types via `Flow<T>` / `suspend fun`. Data sources (local: prebuilt Quran DB, Room, DataStore; remote: none in MVP) are hidden. Mappers translate DAO rows / DataStore values to domain entities.

## Decision

**Option B.**

Rules:

- Repositories live in `:core:data`. Interfaces live in `:core:domain`.
- One repository per aggregate: `QuranRepository`, `BookmarksRepository`, `ReadingPositionRepository`, `SettingsRepository`.
- Data sources (`QuranLocalDataSource`, `BookmarksLocalDataSource`, `SettingsLocalDataSource`) are internal to `:core:data`.
- Mappers (`AyahEntity → Ayah`, etc.) are pure functions, unit-tested.
- Repositories return `Flow<T>` for reactive data (bookmarks, settings, reading position) and `suspend fun` for one-shots (fetch a page).
- No Android imports in domain interfaces — repository interfaces are pure Kotlin.

## Consequences

- Positive: swap-in fakes in tests; add a network source later (translations in POST-MVP) without changing the domain surface.
- Negative: two extra layers (interface + mapper) per aggregate — cheap given how few aggregates the MVP has (~5).
- Follow-up: ADR-0011 (domain layer) and ADR-0012 (UI layer) define the surfaces the data layer serves.

## References

- ADR-0004, ADR-0007, ADR-0011, ADR-0012.
