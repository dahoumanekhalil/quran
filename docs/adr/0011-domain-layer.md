# ADR-0011: Domain layer — thin services, not one class per use case

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 5

## Context and problem statement

Clean Architecture's canonical form uses one class per "use case" (e.g., `GetPageUseCase`, `AddBookmarkUseCase`). This can add ceremony that doesn't buy anything for a small domain.

## Decision drivers

- Simplicity — legible to a single maintainer (charter #16).
- Avoid over-engineering when a thin wrapper adds no value.
- Preserve the ability to compose behavior (some flows really do need coordination).

## Options considered

### Option A — One class per use case

Every domain operation becomes a class with an `invoke()` operator. Aligned with Clean Architecture canon; easy to inject into ViewModels; each use case testable in isolation. For our app, most "use cases" are one-line calls to a repository — a class per line is over-abstract.

### Option B — Thin services + direct repository calls (recommended)

Domain layer has repository *interfaces* (from ADR-0010) plus a small number of domain **services** where coordination across repositories is needed (e.g., `RestoreReadingSessionService` might need `ReadingPositionRepository` and `QuranRepository` together). Simple, single-repository operations are invoked directly by ViewModels via the repository interface.

### Option C — No domain layer

Rejected — breaks ADR-0004 and forces UI to depend on data-layer types.

## Decision

**Option B — thin services.**

Rules:

- `:core:domain` contains:
  - Entities: pure Kotlin data classes (`Surah`, `Ayah`, `Page`, `Juz`, `Bookmark`, `ReadingPosition`, `SettingsSnapshot`).
  - Repository *interfaces* — implementations live in `:core:data`.
  - Domain services — added only when a flow spans multiple repositories or contains non-trivial logic.
- No Android imports. No Compose. No `Context`. Pure JVM.
- ViewModels may call repositories directly for one-shot reads/writes.

## Consequences

- Positive: less boilerplate; ViewModels stay legible; testable domain services when they exist; low ceremony.
- Negative: risk that ViewModels accumulate business logic that should live in the domain — mitigated by code review and by extracting a service the first time a piece of logic is copy-pasted.
- Follow-up: revisit if the domain grows past ~5 services; consider migrating to use-case classes if that happens.

## References

- ADR-0004, ADR-0010, ADR-0012.
