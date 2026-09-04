# ADR-0012: UI layer — stateless screens, hoisted state, `UiState` sealed classes

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 5

## Context and problem statement

The UI layer needs a consistent shape so that every screen is predictable to read, test, and preview.

## Decision drivers

- Screens should be previewable in Compose without a ViewModel (design velocity).
- State survives configuration changes and process death (see ADR-0006).
- Common vocabulary across screens.

## Options considered

### Option A — Composables directly own `mutableStateOf` fields

Simple for tiny components; wrong for screen-level state (loses on config change, no process-death survival).

### Option B — Screen-level Composables observe `UiState` from a ViewModel; leaf Composables are stateless (recommended)

Each screen has:

- `Screen(viewModel: ScreenViewModel = hiltViewModel())` — binds to the ViewModel, collects state, dispatches events.
- `ScreenContent(state: UiState, onEvent: (UiEvent) -> Unit)` — pure stateless Composable. Preview-friendly, unit-testable.
- `UiState` is a `data class` (or `sealed class` with `Loading` / `Empty` / `Success(data)` / `Error` variants when a screen genuinely has those modes).
- `UiEvent` is a `sealed class` naming every user action.

### Option C — Fragment-per-screen with XML

Rejected under ADR-0002.

## Decision

**Option B.**

Rules:

- Every screen has a stateless `ScreenContent(...)` Composable + a stateful `Screen(...)` wrapper that binds the ViewModel.
- `UiState` is immutable; updates produce a new instance.
- `collectAsStateWithLifecycle()` for state observation.
- One-shot effects (snackbars, navigation) via `SharedFlow<UiEvent>` collected in a `LaunchedEffect`.
- No business logic in Composables. If a lambda in a `Screen` grows past a few lines, extract to the ViewModel.

## Consequences

- Positive: `ScreenContent` is Compose-preview-friendly and unit-testable via `createComposeRule`; `UiState` shape is greppable; refactoring is safe.
- Negative: a small amount of duplicated wrapper code per screen — accepted as the cost of consistency.
- Follow-up: convention documented; enforced by code review.

## References

- ADR-0004, ADR-0006, ADR-0011.
- Compose testing: https://developer.android.com/develop/ui/compose/testing
