# Project Charter — Digital Mushaf

**Status:** Adopted 2026-09-04. Reviewed before every phase transition.
**Roadmap:** [`PROJECT_TASKS.md`](../PROJECT_TASKS.md) is the operational plan; this charter is the constitutional layer above it.

---

## Vision

We are building a **digital Mushaf**, not a conventional text-reading application.

When a user opens the application, the intended experience is:

1. Open the app.
2. Immediately return to the Quran at their last reading position.
3. Read comfortably.
4. Turn pages naturally, as if handling a physical Mushaf.
5. Remain undistracted, focused only on the content.

The application must feel: **calm, elegant, minimal, respectful, fast, reliable, timeless, and comfortable for long reading sessions.**

The Quran must always remain the visual and functional center. Every pixel, animation, control, and code decision must serve that center. Anything that competes with the reading experience must be removed, hidden, or redesigned.

**Visual philosophy priority order:** Content > Reading Comfort > Navigation > Controls > Decoration.

There is no dashboard, no engagement funnel, no marketing surface, no gamification, no productivity metaphor. There is only the Mushaf and the smallest, quietest set of tools required to navigate it.

---

## Principles

Non-negotiable. Every engineering decision must honor them.

1. **No advertisements.** Not now, not ever.
2. **No subscriptions.** No paid tiers, no premium features.
3. **No commercial pressure.** No upsells, no prompts to rate/share/upgrade.
4. **No mandatory user accounts.** Anonymous, local-first usage is the baseline.
5. **No tracking.** No third-party analytics SDKs, no behavioral profiling.
6. **No intrusive telemetry.** Any diagnostic collection must be opt-in, minimal, and locally observable.
7. **Full offline operation for core Quran content.** Reading must never require a network.
8. **Quran content integrity is the highest priority.** Content is treated as immutable religious material.
9. **Reading experience takes priority over secondary features.**
10. **Performance matters on low-end devices**, not only flagships.
11. **The application must be extremely stable.** Crashes in the reader are release-blocking.
12. **UI must remain simple and respectful.** No experimental interaction patterns.
13. **External dependencies are introduced carefully**, only when clearly justified.
14. **Every major architectural decision is documented** as an Architecture Decision Record (ADR) in `docs/adr/`.
15. **Every feature must justify its presence.** If it does not serve reading, it does not ship.
16. **Correctness beats cleverness.** Boring, verified code beats novel, unverified code.

---

## Scope

### MVP (v1.0)

- Complete Quran: 114 Surahs, 6236 Ayahs.
- Mushaf-style page-based reading with Madani 604-page pagination.
- Previous / next page navigation.
- Surah list and navigation.
- Direct page navigation.
- Juz navigation.
- Reading position restoration (last-read page + ayah where applicable).
- Bookmarks (add, list, remove, jump).
- Offline Arabic Quran search.
- Correct Arabic RTL rendering with a Mushaf-quality font.
- Light and Dark themes.
- Reading settings (theme, keep screen on, minimal essentials only).
- Fully offline for all MVP features.
- Baseline accessibility (TalkBack labels, adequate touch targets, contrast).
- Responsive layouts for common Android phone form factors.
- Stable state restoration across process death and configuration changes.
- App information / About page (version, dataset source and hash, credits, licenses, privacy statement).

### POST-MVP (v1.1 – v1.3)

- Hizb / Rub markers in navigation.
- Ayah-level bookmarks with notes.
- Reading progress statistics (private, on-device only).
- Verse-of-the-day widget.
- Configurable page-turn animation styles.
- Landscape / tablet-optimized layouts.
- Multiple Mushaf fonts (opt-in).
- Basic haptic feedback for page turns.

### FUTURE (v2+)

- Audio recitation (multiple qaris) with offline downloads.
- Translation and tafsir modules (strictly separated from the Mushaf view).
- Ayah sharing as image cards.
- Bookmark synchronization across devices (only via explicit user-initiated export/import).
- Wear OS companion.

### Explicit non-goals

- Advertising, in-app purchases, subscriptions, paid tiers.
- User accounts as a requirement (they may never be required; optional export/import in future).
- Behavioral analytics, third-party trackers, remote telemetry without opt-in.
- Social sharing designed to drive engagement.
- Gamification (streaks, badges, leaderboards).
- Editing or annotating the Quran text.
- Full-featured tafsir or translation UI competing with the Mushaf view.
- Novel/experimental interaction patterns that increase cognitive load.

---

## Review cadence

Reviewed before every phase transition (see `PROJECT_TASKS.md`). Any change to principles or MVP scope requires an explicit charter revision commit; scope creep into MVP is disallowed and must be logged as a candidate for a later phase.
