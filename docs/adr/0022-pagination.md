# ADR-0022: Madani 15-line 604-page pagination from `quran-center/quran-meta` (Hafs)

- **Status:** Accepted
- **Date:** 2026-09-04 (codifies the pagination reference actually used in Phase 1)
- **Deciders:** dahoumanekhalil
- **Review date:** before every content version bump

## Context and problem statement

The reader is page-based. The 604-page King Fahd Complex Madani mushaf (15 lines per page) is the de facto standard for the printed Uthmani Hafs mushaf, and every ayah must have an authoritative page assignment. We also need juz (30), hizb (60), rub-al-hizb (240), manzil (7), ruku (556), and sajda (15) mappings.

## Decision drivers

- Charter #8 — page assignments are as sacred as the text itself; a misassigned page shows the wrong opening word at the top of a page (a very visible defect).
- License compatibility with a free app.
- Cross-referenced against the KFGQPC source directly or via an equivalent.
- Machine-readable format.
- Complete set (page + juz + hizb + rub + manzil + ruku + sajda in one dataset).

## Options considered

### Option A — Hand-transcribe from a printed Madani mushaf

Highest control, lowest confidence. Rejected: risk of typos across 6236 assignments is unacceptable.

### Option B — Extract from `quran-center/quran-meta` (Hafs riwaya, `HafsLists.ts`) (recommended)

MIT-licensed. Cross-checked (per the upstream docs) against Tanzil, Quran.com API, AlQuran Cloud, KFGQPC font datasets, and Quranpedia. Complete set: HizbQuarterList (240 rubs), JuzList (30), PageList (604), ManzilList (7), RukuList (556), SajdaList (15), SurahList (114 with name, ayah count, revelation order, ruku count, meccan/medinan flag).

### Option C — Reconstruct from KFGQPC PDFs or images

Would require OCR / manual transcription of 604 pages. Rejected.

## Decision

**`quran-center/quran-meta` `HafsLists.ts` (Option B).**

Vendored at commit `a5dd4a46dc6f7830a4303e89c3b4b3a15a213ac9` under `data/sources/quran-meta/repo/`, package version `6.1.1-6`. The pipeline extracts the arrays via regex (no Node runtime needed).

## Consequences

- Positive: complete, MIT-licensed, cross-verified upstream; matches the printed Madani mushaf; extends easily to other riwayas in POST-MVP.
- Negative: takes on a dependency that isn't the KFGQPC directly — mitigated by the upstream's own cross-validation and by our Phase 1 structural validation suite (TASK-019) which enforces per-surah ayah counts and the 604/30/60/240/15 invariants.
- Follow-up: if a page boundary defect is ever discovered, file a `content-v1.0.1` bug and re-run Phase 1.

## References

- Provenance: `data/sources/quran-meta/SOURCE.json`.
- Data model: `docs/data-model.md`.
- Related: ADR-0021.
