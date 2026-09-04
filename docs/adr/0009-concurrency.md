# ADR-0009: Kotlin Coroutines + Flow for concurrency

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 5

## Context and problem statement

The app performs disk IO (Room, prebuilt SQLite asset queries, DataStore), potentially heavier CPU work (search normalization, page pre-loading), and needs to expose reactive updates to Compose. It must not block the main thread — jank in the reader is unacceptable (charter #11).

## Decision drivers

- Structured concurrency (child scopes, cancellation cascades).
- Reactive state to feed Compose (`StateFlow.collectAsStateWithLifecycle`).
- Simple, idiomatic Kotlin.
- Test tooling (`Turbine`, `runTest`).

## Options considered

### Option A — Kotlin Coroutines + Flow

Native language integration, structured concurrency, `Dispatchers.IO` / `Default` / `Main`, `StateFlow` / `SharedFlow` for hot streams, `Flow` for cold. Compose integration via `collectAsStateWithLifecycle()`.

### Option B — RxJava 3

Mature, powerful operators, but heavier API surface, less idiomatic in Kotlin, extra dependency (~2 MB), less integrated with Compose.

### Option C — `Executor` / manual `Thread` / callbacks

Rejected — reinventing structured concurrency and Flow.

## Decision

**Kotlin Coroutines + Flow (Option A).**

Rules:
- IO on `Dispatchers.IO` (Room, prebuilt DB queries, DataStore reads/writes, file IO).
- CPU-heavy on `Dispatchers.Default` (normalization, search query construction).
- UI updates on `Dispatchers.Main.immediate` (implicit in Compose).
- Never launch on the "unscoped" `GlobalScope`. All scopes must be structured (ViewModelScope, an application-scoped `CoroutineScope` in the DI graph, or explicit child scopes).

## Consequences

- Positive: idiomatic; cancellation and error propagation for free; excellent test story (`runTest`, `Turbine`).
- Negative: coroutines have a learning curve for those new to them (exception propagation, dispatcher choice) — mitigated by conventions in code review.
- Follow-up: TASK-030 pins coroutine / Flow versions in `libs.versions.toml`.

## References

- Kotlin coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- Compose + Flow: https://developer.android.com/develop/ui/compose/state#use-other-types-of-state-in-jetpack-compose
