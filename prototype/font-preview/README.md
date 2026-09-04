# Font preview (browser)

Self-contained HTML file that renders 7 target mushaf pages (1, 2, 3, 50, 300, 600, 604) with both candidate Mushaf fonts embedded as base64 data URIs. **Opens directly in any modern browser** — no server, no build step, no Android required.

## Usage

```bash
python build.py           # regenerates index.html from the frozen corpus
open index.html           # macOS / xdg-open on Linux / double-click on Windows
```

Or copy `index.html` to your phone (any file manager) and open it in the phone's browser to preview the fonts at real device pixel density.

## Purpose

Bridges the gap between TASK-025 (font selected) and TASK-026 (Android prototype). It lets you validate that:

- Both fonts cover the corpus without missing glyphs (visual check to complement the automated cmap test).
- The Uthmani orthography (superscript alef, alef wasla, small high marks, sajdah symbol, rub-al-hizb marker) renders correctly.
- The overall typographic "feel" matches a printed Madani mushaf reference.

## Not a substitute for TASK-026

This is a browser preview, not the Android app. It uses the platform's HTML text renderer (Blink / WebKit), not Android's `TextView` / Compose text stack. Rendering can differ. TASK-026 (the actual Android prototype) is still required to satisfy the roadmap's acceptance criteria for Phase 2.
