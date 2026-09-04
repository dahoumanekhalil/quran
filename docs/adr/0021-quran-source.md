# ADR-0021: Tanzil Project Uthmani v1.1 as the Quran text source

- **Status:** Accepted
- **Date:** 2026-09-04 (codifies the source selection actually executed in Phase 1)
- **Deciders:** dahoumanekhalil
- **Review date:** before every content version bump

## Context and problem statement

The Quran text is the app's core content. It must be authoritative, verifiable, freely redistributable in a non-commercial app, and importable byte-for-byte into our pipeline (charter #8).

## Decision drivers

- Charter #8 — content integrity is the highest priority.
- Provenance — an auditable source with a documented change log.
- License compatibility with a free (no-ads, no-subs) Android app.
- Coverage of Uthmani orthography (superscript alef, alef wasla, small high seen, sajdah symbol, rub-al-hizb marker).
- Cross-source verifiability — a second independent source available for diffing.

## Options considered

### Option A — Tanzil Project, "Uthmani" script variant, v1.1 (recommended)

Public XML/TXT/SQL formats from https://tanzil.net/. Version 1.1 released Feb 2021. License: CC BY 3.0 with "verbatim only" clause (matches our immutability contract). Widely used across Islamic apps (AlQuran Cloud, Quran.com internal use, many others). Well-documented change log. Full Uthmani marks available.

### Option B — King Fahd Complex (KFGQPC) text

The most authoritative source (the printer of the Madani mushaf). Requires the KFGQPC font pairing to render correctly. Distribution outside their own applications is less clear; not as easily downloadable in a machine-readable form. Used as our **cross-source verification** input via the Quran.com API (which mirrors KFGQPC).

### Option C — QuranComplex / other GitHub mirrors

Most are Tanzil-derived (not independent). No provenance improvement over A.

## Decision

**Tanzil Project Uthmani v1.1 (Option A).**

Concrete download parameters (recorded in `data/sources/tanzil-uthmani/1.1/SOURCE.json`):
`quranType=uthmani`, `marks=true`, `sajdah=true`, `rub=true`, `tatweel=true`, `outType=xml`.

**Cross-source verification** against **Quran.com API v4** (`/api/v4/quran/verses/uthmani`, KFGQPC-derived, independent lineage). Phase 1 diff: 6236/6236 verses match on consonantal skeleton; 6126/6236 byte-identical; 110 diacritic-only differences documented and accepted as expected tradition variance.

## Consequences

- Positive: high-provenance text, permissive license, verified against an independent lineage, no runtime dependency on Tanzil or Quran.com — the frozen artifact ships in the APK.
- Negative: Tanzil's "verbatim only" clause requires the app to preserve the exact bytes and attribute the Tanzil Project visibly (see `NOTICE`, `LICENSES/CC-BY-3.0.txt`). Enforced by TASK-017 (byte-preserving import) and TASK-024 (Content Integrity Gate).
- Follow-up: Any future content version bump requires a full Phase 1 pipeline re-run and a new ADR entry (`ADR-0021.1`, etc.) or supersession.

## References

- Content manifest: `data/output/RELEASE/CONTENT_MANIFEST.md`.
- Provenance: `data/sources/tanzil-uthmani/1.1/SOURCE.json`.
- Cross-source report: `data/output/intermediate/cross-source-diff-report.md`.
