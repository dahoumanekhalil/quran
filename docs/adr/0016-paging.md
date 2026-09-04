# ADR-0016: Compose `HorizontalPager` for the Mushaf page-turn interaction

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** at Phase 2 rendering acceptance (TASK-029)

## Context and problem statement

The reader is fundamentally page-based (604 pages). Users swipe horizontally to turn pages. The paging component must be RTL-aware (Arabic → previous is to the right), smooth on low-end devices, and preserve position across process death.

## Decision drivers

- RTL awareness (LayoutDirection.Rtl → next page is to the left).
- Smoothness on low-end devices (60fps target, <100 ms page turn budget per TASK-026).
- Simplicity — few Composables to reason about.
- Fit with ADR-0002 (Compose).

## Options considered

### Option A — `HorizontalPager` (Compose Foundation, non-experimental since 1.4)

Idiomatic Compose. `PagerState` exposes `currentPage`, `settledPage`, snapping, page count, and RTL-aware direction via the enclosing `LayoutDirection`. Rememberable across configuration changes.

### Option B — `ViewPager2` wrapped in `AndroidView`

Mature (years in production), but wrapping into Compose costs the Compose state advantages and adds fragment/adapter boilerplate for what should be a simple pager.

### Option C — Custom paged renderer (`LazyList` + snap + gesture handling)

Maximum control. Reinvents `HorizontalPager` for no clear win.

## Decision

**`HorizontalPager` (Option A).**

Wrapped in a `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` for the reader hierarchy so that swipes and animations follow Arabic reading direction naturally.

`PagerState.currentPage` is persisted to `SavedStateHandle` for process-death survival (see ADR-0006). Pre-loading of ±1 page is via the default off-screen page count.

## Consequences

- Positive: idiomatic; RTL handling is a `LayoutDirection` set-and-forget; state persistence works out of the box; page-transition animations are Compose-native.
- Negative: If TASK-027 / TASK-028 show performance regressions on low-end devices we may need to add explicit page-content memoization or move to a `LazyList` + custom snap — validated in Phase 2.
- Follow-up: TASK-026 uses HorizontalPager; TASK-029 signs off or triggers a return here.

## References

- HorizontalPager: https://developer.android.com/develop/ui/compose/layouts/pager
