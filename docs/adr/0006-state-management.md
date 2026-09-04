# ADR-0006: ViewModel + StateFlow + immutable UI state class

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 5

## Context and problem statement

Compose screens need a source-of-truth for their UI state that survives configuration changes and, for user-critical values (last-read page, current search query), process death.

## Decision drivers

- Reader-critical state (last-read page, scroll position, bookmark set) must survive process death without a user-visible reset (charter #11).
- State updates from IO (Room, prebuilt SQLite asset) must not block the main thread.
- Testability without Android runtime (ViewModels should be JVM-testable).

## Options considered

### Option A — `ViewModel` + `StateFlow<UiState>` + `SavedStateHandle`

Standard Google-recommended pattern. `StateFlow` is a hot, always-latest flow. Screens observe via `collectAsStateWithLifecycle(...)`. `SavedStateHandle` persists lightweight scalar state through process death; heavy data is re-fetched from the repository on restart.

### Option B — MVI store (as in ADR-0004)

Rejected there.

### Option C — Compose-only state (raw `mutableStateOf` at screen level, no ViewModel)

Loses ViewModel scope, breaks on rotation without extra work, no `SavedStateHandle`. Only appropriate for trivial UI components.

## Decision

**Per-screen `ViewModel`** exposing:

- `val uiState: StateFlow<UiState>` — the sole state source, an immutable `data class` or `sealed class`.
- User events either as typed function calls (`onPageChanged(page: Int)`) or a single `onEvent(event: ScreenEvent)` dispatcher — pick the simplest per screen.
- One-shot events (snackbars, navigation) via `SharedFlow<UiEvent>` collected with `LaunchedEffect`.
- `SavedStateHandle` for values needed to reconstruct after process death (last-read page, current selection).

Screens use `collectAsStateWithLifecycle()` to avoid collecting when the screen is stopped.

## Consequences

- Positive: standard, testable, plays well with Compose recomposition; heavy state is not kept alive across process death (repositories are the source of truth).
- Negative: requires discipline — `UiState` must remain a data class, not accumulate mutable fields. Enforced via code review and lint rules where possible.
- Follow-up: ADR-0012 (UI layer) elaborates on `UiState` shape conventions.

## References

- Android state guidance: https://developer.android.com/topic/architecture/ui-layer/state-production
