# ADR-0005: Navigation Compose for in-app navigation

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** before Phase 6

## Context and problem statement

The MVP has few destinations: the Reader (page-based Mushaf view), a Surah/Juz/Page navigation surface, a Bookmarks screen, a Search screen, and Settings/About. Deep links (jump to a page or ayah) must survive process death.

## Decision drivers

- RTL-aware transitions (charter Arabic-content focus).
- Type-safe destinations with parameters (page number, ayah global index).
- State restoration across process death (charter #11).
- Fits ADR-0002 (Compose) idiomatically.
- Small dependency footprint (charter #13).

## Options considered

### Option A — Navigation Compose (`androidx.navigation:navigation-compose`)

Google's official Compose-first navigation. Type-safe destinations, save/restore across process death via `SavedStateHandle`, supports nested graphs, integrated with Compose animation APIs.

### Option B — Voyager / Decompose (community libs)

Popular, sometimes cleaner APIs. But: extra dependency, smaller community, uncertain long-term support. Charter #13 argues against.

### Option C — Custom in-app navigation (state-driven `when`)

Cheapest — a `when(currentDestination)` in the root Composable. Works fine for 5 screens. Downsides: reimplements back-stack, transitions, deep links, and state restoration.

## Decision

**Navigation Compose (Option A).**

The MVP has 5–6 destinations plus deep links — the middle-ground where Nav Compose earns its keep. Type-safe routes via the `@Serializable` / typed destinations API (introduced in Nav Compose 2.8) keep destination signatures compile-checked.

## Consequences

- Positive: standard patterns, official support, `SavedStateHandle` integration for process-death survival.
- Negative: adds a moderate dependency (~200 KB). Type-safe destinations require Kotlin serialization plugin.
- Follow-up: TASK-030 wires Nav Compose in the initial Gradle config; TASK-030+ defines the destination graph.

## References

- Navigation Compose: https://developer.android.com/develop/ui/compose/navigation
- Type-safe navigation (Compose): https://developer.android.com/guide/navigation/design/type-safety
