# ADR-0015: Bundle a single Mushaf OTF font in the APK (MVP)

- **Status:** Accepted
- **Date:** 2026-09-04
- **Deciders:** dahoumanekhalil
- **Review date:** after Phase 2 rendering acceptance (TASK-029)

## Context and problem statement

The reader's typography is central (charter — Content > Reading Comfort > everything else). We need a way to ship a Mushaf-quality Arabic font that is always available, at consistent quality across devices, and that permits Google Play redistribution.

## Decision drivers

- Consistent rendering across devices (system Arabic fonts vary).
- Offline (charter #7): no runtime font download.
- License compatibility with a freely-distributed Android app.
- Install size (the primary Mushaf font is usually 1–2 MB).
- Ability to add alternate fonts POST-MVP without restructuring.

## Options considered

### Option A — Bundle a single primary OTF/TTF in `assets/fonts/` (recommended)

Ships with the app. Loaded via `Font(file = ...)` in Compose or `ResourcesCompat.getFont(...)`. Simple, offline, consistent.

### Option B — Downloadable Fonts (Google Fonts provider)

Zero install-size impact. Requires network on first use — violates charter #7 for the primary reading font. Not applicable for a font that is a first-launch requirement.

### Option C — Rely on the system Arabic font

Zero size, but zero control over quality — Samsung, Xiaomi, Pixel, and OEM builds ship different Arabic fonts. Unacceptable for a Mushaf-quality bar.

## Decision

**Option A — bundled OTF, single primary font in MVP.**

Alternate fonts (POST-MVP): add additional TTFs to the assets and expose a Settings toggle. Font file placed in `android/app/src/main/assets/fonts/` and loaded via Compose `Font(...)`.

The specific font is chosen in **TASK-025** (Phase 2), with candidates including KFGQPC Uthman Taha Naskh, Amiri Quran (SIL OFL — safest license), and equivalents. The chosen font's full license text is added to `LICENSES/` and referenced from `NOTICE`.

## Consequences

- Positive: consistent, offline-first, fully controllable typography.
- Negative: install size grows by 1–2 MB — trivial. Any future font swap requires an app update — acceptable for MVP.
- Follow-up: TASK-025 fixes the font choice, `ADR-0024 (font)` will be written there.

## References

- ADR-0013, ADR-0017.
- Compose fonts: https://developer.android.com/develop/ui/compose/text/fonts
- SIL Open Font License 1.1: https://openfontlicense.org/
