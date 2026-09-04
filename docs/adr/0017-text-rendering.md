# ADR-0017: Render ayah text with Compose `BasicText`, fall back to wrapped `TextView` if defects appear

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** at Phase 2 rendering acceptance (TASK-029)

## Context and problem statement

The most difficult technical bet in the project is Arabic Uthmani text rendering matching a Madani mushaf reference on the target device matrix. The rendering pipeline must handle: complex-script shaping, superscript alef marks, sajdah symbol, alef wasla, small high marks, and multi-line justification.

## Decision drivers

- Rendering correctness (charter #8).
- Consistency with the physical Madani reference (TASK-026 acceptance criteria).
- Performance (charter #10, #11): 60fps swipe, <100ms page turn on low-end devices.
- Ability to fall back at low cost if the primary choice shows defects.

## Options considered

### Option A — Compose `BasicText` (recommended primary)

Compose text uses the same platform text stack that `TextView` uses under the hood (Skia + Android's `TextLayout` — via `androidx.compose.ui.text` in Compose). Arabic RTL and shaping have been mature since Compose 1.2. Fully composable, integrates with Compose animations and state.

### Option B — `AndroidView` wrapping a native `TextView`

Historically the most battle-tested Arabic renderer on Android. Slightly heavier per-composition than `BasicText`. Small wrapping overhead.

### Option C — `Canvas.drawText` with a custom text layout

Maximum control, most work, reimplements shaping. Reject unless A and B both fail.

## Decision

**Compose `BasicText` (Option A) as the primary rendering surface**, with **`AndroidView(::TextView)` (Option B) as the pre-approved fallback** if TASK-026..028 finds shaping defects on any device in the matrix.

## Consequences

- Positive: idiomatic, animation-friendly, no wrapper overhead. If A works, we ship simpler code.
- Negative: if we fall back to B for the ayah surface, the reader will mix Compose (chrome, controls) with AndroidView (ayahs) — the hybrid is well-supported and does not require superseding ADR-0002.
- Follow-up: TASK-026 renders pages 1/2/3/50/300/600/604 using A; side-by-side compared with a printed Madani reference. TASK-029 signs off Option A or authorizes the fallback.

## References

- Compose text: https://developer.android.com/develop/ui/compose/text
- ADR-0002, ADR-0016.
