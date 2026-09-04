# ADR-0002: Use Jetpack Compose as the UI toolkit

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** after Phase 2 rendering acceptance (TASK-029)

## Context and problem statement

Android offers two UI toolkits: the classic View system (`TextView`, XML layouts) and Jetpack Compose (declarative Kotlin UI). The reader engine's rendering quality is the project's highest technical risk (see PROJECT_TASKS.md Phase 2), so the toolkit must handle Arabic RTL and Mushaf-quality Uthmani shaping reliably on low-end devices.

## Decision drivers

- Arabic RTL support and Uthmani shaping correctness (critical — see charter #8).
- Rendering performance for 604 pages with large paragraphs of complex-script text on low-end devices.
- Ease of building the reader's page-turn interaction (`HorizontalPager` in Compose vs `ViewPager2` in Views).
- Idiomatic fit with Kotlin (ADR-0001).
- Long-term maintainability; Google's investment direction.
- Ability to fall back if the chosen toolkit shows shaping defects in the Phase 2 prototype.

## Options considered

### Option A — Jetpack Compose

Declarative, idiomatic Kotlin, official direction for new Android UIs, `HorizontalPager`/`LazyColumn`/animation APIs built in. RTL support is mature (LayoutDirection propagates), and Compose's `Text` / `BasicText` uses the platform text renderer (Skia + Android text stack) so Arabic shaping is delegated to the same shaper the classic `TextView` uses.

### Option B — Classic Views (XML + `TextView`)

The `TextView` has years of production hardening for Arabic. Established RTL support since Android 4.2. Downsides: XML layouts are verbose, ViewPager2 + fragment lifecycle for paging is heavier, mixing with Kotlin idioms less clean, industry momentum is away from it, and it doesn't affect the underlying shaping — Compose Text uses the same platform text pipeline.

### Option C — Hybrid (Compose scaffolding + `AndroidView(TextView)` for ayah text)

Use Compose everywhere except for the page text, which is drawn by a wrapped `TextView`. Belt-and-braces if Compose Text ever shows a shaping issue.

## Decision

**Jetpack Compose (Option A).** Fall back to Option C (wrapped `TextView`) *only* if the Phase 2 rendering prototype (TASK-026..028) exposes shaping defects on the target device matrix.

Compose is chosen because RTL and Arabic shaping now go through the same platform pipeline that `TextView` uses — the shaping-risk delta between Compose and Views is small, while the productivity, animation, and paging ergonomics gap is large.

## Consequences

- Positive: idiomatic Kotlin UI; `HorizontalPager` handles the reader's core interaction; state hoisting matches ADR-0006; less boilerplate.
- Negative: Compose adds a runtime overhead vs Views on very old devices — acceptable at minSdk 24 (see ADR-0003) but validated in Phase 2 on low-end hardware.
- Follow-up: TASK-026 explicitly renders pages 1/2/50/300/604 with the chosen approach; TASK-029 confirms acceptance or triggers a return to this ADR.
- If ADR-0017 (text rendering) is later flipped to `AndroidView(TextView)` for the ayah surface, this ADR does NOT need to be superseded — the hybrid is compatible.

## References

- ADR-0001, ADR-0016 (paging), ADR-0017 (text rendering), ADR-0023 (rendering acceptance in Phase 2).
