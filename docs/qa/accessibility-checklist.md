# Accessibility validation checklist — TASK-236

Runs on your phone. See `PROJECT_TASKS.md` TASK-160 for the code-level work already done.

## 1. Google Accessibility Scanner sweep

Install "Accessibility Scanner" by Google LLC. Take a snapshot on each screen.

| Screen | Snapshot taken | Critical issues (red) | Notes |
|---|---|---|---|
| Reader (chrome visible) | [ ] | | |
| Reader (chrome hidden) | [ ] | | |
| Navigation — Surahs | [ ] | | |
| Navigation — Juz | [ ] | | |
| Navigation — Page jump | [ ] | | |
| Navigation — Saved | [ ] | | |
| Search — empty state | [ ] | | |
| Search — with results | [ ] | | |
| Settings | [ ] | | |
| About | [ ] | | |

Target: **zero red items**. Orange/blue items are nice-to-haves.

## 2. TalkBack walk

Enable Settings → Accessibility → TalkBack. Read the built-in tutorial once (gestures change).

Walk each screen. Confirm:

| Element | Expected announcement | Result |
|---|---|---|
| Reader page body | "Reading page N of 604. Surah X. Juz Y. Swipe left for next page, right for previous. Tap the center to toggle controls." | [ ] |
| Reader ayah text (Arabic) | **Silent** — the ayah body is intentionally not spoken (charter: no TTS on Quranic text) | [ ] |
| Menu icon (top left) | "Navigation, button" | [ ] |
| Search icon | "Search, button" | [ ] |
| Bookmark icon (outlined) | "Add bookmark, button" | [ ] |
| Bookmark icon (filled) | "Remove bookmark, button" | [ ] |
| Surah list row | "Al-Fatihah — The Opening, 7 ayahs, double-tap to open Al-Fatihah" | [ ] |
| Juz list row | "Juz 1, starts at surah 1, ayah 1 (page 1), double-tap to open Juz 1" | [ ] |
| Bookmark row | "Page 42, saved <date>, double-tap to open page 42" | [ ] |
| Search field | Announces "Search the Mushaf" as a text-input label | [ ] |
| Search result | "Surah name, page/juz info, ayah text, double-tap to open page N, ayah S:A" | [ ] |
| Settings — Theme radio | "Light" / "Dark" / "Follow system" each selectable | [ ] |
| Settings — Text size radio | "Small" / "Medium" / "Large" | [ ] |
| Settings — Keep screen on switch | "Keep screen on while reading, on/off" | [ ] |
| About — Back arrow | "Back, button" | [ ] |

**Turn TalkBack OFF when done** (Settings → Accessibility → TalkBack).

## 3. Arabic locale walk-through

Settings → System → Languages → move Arabic (العربية) to the top.

| Check | Result |
|---|---|
| Whole app flips to RTL layout | [ ] |
| About screen back arrow points **right** (auto-mirrored) | [ ] |
| Settings row chevron points **left** (auto-mirrored) | [ ] |
| Reader chrome layout stays the same (menu-left / search-right hardcoded via LTR wrapper — this is intentional) | [ ] |
| Reader page swipe direction still: visual-left = next page | [ ] |
| Navigation tabs read right-to-left visually | [ ] |

Restore English at the top when done.

## 4. Reduced motion

Settings → Accessibility → Text and display → **Remove animations** → ON. Then:

| Check | Result |
|---|---|
| Reader chrome fade-in / fade-out becomes instant (no animation) | [ ] |
| Reader page-turn animation still runs (this IS the interaction, not decoration) | [ ] |

Restore normal animations.

## 5. Large font scale (system-level)

Settings → Display → Font size → set to **Large** or **Largest**.

| Check | Result |
|---|---|
| UI chrome (Settings, Navigation, About, Search) text scales up | [ ] |
| Reader Mushaf text does **not** scale (intentional — see TASK-146; scales via app's own Small/Medium/Large picker) | [ ] |
| No text is clipped or overlaps on any screen | [ ] |

Restore Default.

## Signing off

When every box in this file is checked → mark TASK-236 Completed and move to Phase 17.
