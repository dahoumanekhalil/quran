# ADR-0004: Clean-layered MVVM with unidirectional data flow

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 5

## Context and problem statement

The app must be maintainable by a small team, easy to test, and stable across configuration changes and process death. The architecture pattern shapes every screen, every ViewModel, and every test.

## Decision drivers

- Testability of business logic without touching Android APIs.
- State survival across configuration changes and process death (charter #11).
- Small learning surface — the codebase should be legible to a single maintainer.
- Compatibility with Jetpack Compose (declarative, reactive).
- Predictable data flow — no "who mutated this" ambiguity.

## Options considered

### Option A — MVI (Model-View-Intent) with a store

Fully unidirectional; every user action produces an intent that reduces state through a single function. Excellent for complex screens with many concurrent user actions. Adds ceremony and vocabulary (intents, reducers, side-effects) for simple screens.

### Option B — MVVM with unidirectional data flow (recommended)

Screens observe an immutable `UiState` from a ViewModel. User actions call typed ViewModel functions or an `onEvent(...)` dispatcher. ViewModels expose `StateFlow<UiState>` and `SharedFlow<UiEvent>` for one-shot events. Simple, testable, matches Google's official Android architecture samples.

### Option C — Classic MVP or activity-owned state

Rejected: known to leak, awkward with Compose, poor process-death survival.

## Decision

**Clean-layered MVVM with unidirectional data flow (Option B).** Layers: `ui` (Compose screens + ViewModels) → `domain` (entities, repository interfaces, thin services) → `data` (Room, prebuilt SQLite asset, DataStore).

Data flows **one direction: data → domain → ui**. Events flow the other direction: `ui` calls typed ViewModel functions, which invoke domain services / repositories. UI never touches persistence directly.

## Consequences

- Positive: low ceremony, high testability, matches official Google samples, clean separation lets domain and data layers be pure Kotlin (no Android imports).
- Negative: an MVI store could win on very interactive screens; not our profile — a reader is mostly display + a small navigation surface.
- Follow-up: ADR-0006 (state management) codifies StateFlow + immutable state. ADR-0010/0011/0012 define per-layer rules.

## References

- Google's Android architecture guidance: https://developer.android.com/topic/architecture
- Related ADRs: ADR-0006, ADR-0010, ADR-0011, ADR-0012.
