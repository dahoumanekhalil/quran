# ADR-0026: Mushaf line-layout source — QCF4 for `(page, line)` mapping only

- **Status:** Accepted (2026-09-07). Migration executed: content v1.0.0 → v1.1.0.
- **Date:** 2026-09-06 (proposed) / 2026-09-07 (accepted)
- **Deciders:** dahoumanekhalil
- **Review date:** before every content version bump
- **Supersedes:** none. Complements ADR-0021 (text source), ADR-0022 (pagination), ADR-0024 (font), ADR-0017 (text rendering)

## Context and problem statement

The Reader needs to reproduce the Madani mushaf line geometry — i.e. know exactly **which words are on which line of which page**. Our current dataset (ADR-0021 Tanzil Uthmani v1.1 + ADR-0022 quran-meta pagination) knows which ayah is on which page, but does not know which words fall on line 7 vs line 8 of that page. Without that data, the Android renderer would either invent line breaks (violates project charter #8 — page integrity) or resort to justified text flow (visually incompatible with a real printed mushaf).

This ADR adopts an **additional** dataset — QCF4 — as the authoritative source for `(surah, ayah, position_in_ayah) → (page, line)` mapping. Tanzil remains the sole authoritative Quran text source. QCF4's own text and fonts are NOT redistributed.

## Decision drivers

- **Charter #1 — content integrity is non-negotiable.** No mutation of Tanzil text. No silent normalization. No adoption of a different orthographic edition of the text.
- **Charter #8 — page assignments are as sacred as the text itself.** Same principle extends to line assignments.
- **License compatibility with an offline free app.** Anything shipped must be redistributable under a clearly documented license.
- **Fixed Madani 604-page model already codified in ADR-0022.** Must remain intact.
- **Existing KFGQPC Uthmanic Hafs v2.2 font (ADR-0024) is the rendering font.** No font swap.
- **Verifiable deterministic mapping** — every Tanzil word must map to exactly one `(page, line)` with no manual per-word patching.

## Options considered

### Option A — QUL layout ID 10 (KFGQPC V2 1421H print) via blueheron786 GitHub mirror

Pros: matches KFGQPC V2 edition semantically; SQLite is compact (~100 KB); contains only position pointers, no text.
Cons: **no license file** (mirror), so default GitHub "all rights reserved" applies; QUL upstream's own resource-level license is not surfaced (per-resource, no visible terms); the pointer-only structure requires a separate word-text dataset (QUL resource 61 QPC V2 word-by-word), which uses PUA glyph codes rendered by 604 per-page fonts — architecturally incompatible with our single-font Unicode text (ADR-0024). Rejected on licensing.

### Option B — `zonetecde/mushaf-layout` GitHub JSON

Pros: standard Unicode Uthmani word text alongside layout; word-by-word validation showed the closest textual match to our Tanzil (Class A tokenization difference: 7 standalone waqf tokens per longer page; Class B small rasm marks: a small number of pages).
Cons: **no LICENSE file, no provenance statement, no fidelity claim in the README.** Default GitHub terms apply. Rejected on licensing.

### Option C — `MohamadHajjRabee/quran-qcf4` (chosen)

Pros:
- **MIT license** on the JSON layout data (`LICENSE.md` verified; © 2026 Mohamad Hajj Rabee).
- Complete 604-page/9046-line layout with per-line word records and `verse_key:position` addressing.
- Structural fields we need: `page`, `line_number`, `line_type`, `is_centered`, `verse_key`, `position` — matches the semantic model in ADR-0022.
- Provenance stated: font & glyph design by Uthman Taha, production by King Fahd Complex, base "Madinah Mushaf 1441 AH".
- Available via GitHub, npm and Hugging Face; a stable git tag is easy to pin.

Cons:
- QCF4's `text` field uses **Simple Uthmani** orthography (plain alif U+0627 instead of wasla alif U+0671; missing dagger alifs; different tanwin representations). Byte-level word text does NOT match Tanzil Uthmani. **We resolve this by not using QCF4's text values at all** — we consume only the structural fields.
- QCF4 layout is calibrated for the QCF4 per-page glyph fonts (47 PUA fonts). When rendered with our KFGQPC v2.2 unified Naskh font, horizontal justification inside a line will differ from the 1441H printed page even though line ASSIGNMENTS (which words on which line) remain authentic.
- The layout represents the **1441H print**. Our current `ayahs.page` values come from `quran-center/quran-meta` (Hafs), which reflects an earlier print. In a small number of ayahs the two disagree on which page an ayah is on (measured below).

### Option D — Author our own layout dataset

Rejected. 604 pages × 15 lines × word-by-word transcription is high risk for content-integrity errors, requires expert review, and duplicates work already released under MIT by QCF4.

### Option E — Ship without line-level fidelity (text-flow renderer)

Rejected on Charter #8: line breaks that depend on screen width mean the reader would never match a physical page. This is exactly the "invented line breaks" case the project explicitly refuses.

## Decision

**Adopt QCF4 as an additional data source for line-level layout, restricted to structural fields only. Tanzil remains the sole text authority. KFGQPC v2.2 remains the sole font.**

### What we redistribute from QCF4

- The `pages/*.json` files (604 files), **transformed at build time** into two new SQLite tables — `page_lines` and `page_words` — from which only the following QCF4 fields are retained:
  - `page` (integer 1..604)
  - `line_index` (integer 1..15, or 1..8 on pages 1–2)
  - `line_type` (`ayah`, `surah_name`, `basmalah`)
  - `is_centered` (0/1)
  - `surah_number` (for `surah_name` / `basmalah` line types)
  - Per-word structural `(surah, ayah, position_in_ayah_qcf4)` used solely as an ordinal index within an ayah for reading-order alignment against Tanzil.
- We do **not** ship: QCF4's `text` field, `char` field, `code` field, `font` field, `qbsml.json`, `font-map.json`, `verses.json` (redundant with our own metadata), any QCF4 font file (`fonts/*.ttf`, `fonts-woff2/*.woff2`), or the sajda-decoration placeholder token `#1969`.
- Attribution and MIT license text are added to `NOTICE`.

### Text authority

Tanzil Uthmani v1.1 remains authoritative. The `ayahs.text_uthmani` column is byte-identical between content v1.0.0 and content v1.1.0. Every Tanzil token gets a `(page, line)` assignment; QCF4's word text is used **only** as a positional ordinal, never rendered or compared for byte equality.

### Font authority

KFGQPC Uthmanic Hafs v2.2 (ADR-0024). No QCF4 font is shipped or used.

### Mapping and reconciliation rules

Per-ayah sequential alignment. For each ayah:

1. Walk Tanzil `text_uthmani.split()` tokens in order.
2. For each **non-waqf** token, consume the next QCF4 word entry in `(page, line, position)` reading order. Record `(page, line)`.
3. For each **waqf-only** token (single codepoint in U+06D6..U+06ED, excluding U+06DE): apply **backward-attach rule** — inherit `(page, line)` from the previous Tanzil non-waqf token in the same ayah.
4. For each **U+06DE (۞ Rub el-Hizb) marker**: apply **forward-attach rule** — inherit `(page, line)` from the next Tanzil non-waqf token in the same ayah. Rationale: `۞` sits at the START of an ayah in Tanzil (whereas backward-attach would be undefined at ayah start), and QCF4 encodes hizb-quarters as a separate line marker, so `۞` visually anchors to the ayah it introduces.

## Full-mushaf validation (measured)

Validation script: `tmp_full_validation.py`. Inputs: `data/output/RELEASE/quran-1.0.0.db` (Tanzil, frozen) + `MohamadHajjRabee/quran-qcf4` commit `5130511027e769f0a8f4eeb7f00f46bde3788d60` (2026-08-11).

### Totals

| Metric | Value |
|---|---|
| Pages checked | 604 / 604 |
| QCF4 total lines | 9046 |
| QCF4 word entries | 77 448 |
| Tanzil total tokens | 82 011 |
| Tanzil non-waqf tokens | 77 433 |
| Tanzil waqf-only tokens (U+06D6..U+06ED excluding U+06DE) | 4 379 |
| Tanzil Rub el-Hizb tokens (U+06DE) | 199 |
| Sequential mappings (non-waqf) | **77 433 / 77 433 = 100.00 %** |
| Waqf-rule backward mappings | **4 379 / 4 379 = 100.00 %** |
| Rub-rule forward mappings | **199 / 199 = 100.00 %** |
| Unmapped Tanzil tokens after all rules | **0** |
| Unmapped QCF4 real words (edition delta) | **0** |
| Unmapped QCF4 sajda-marker placeholders (`#1969`, decoration) | 15 |

### Per-page distribution

| Metric | Value |
|---|---|
| Pages with real edition delta (non-waqf word mismatch) | **0 / 604** |
| Pages requiring waqf-rule backward attach | 587 / 604 |
| Pages requiring rub-rule forward attach | 71 / 604 (approx; one per Rub boundary) |
| Pages containing a QCF4 sajda-marker placeholder (safely dropped) | 15 / 604 |
| Max words affected on any single page | 20 (page 35) |

### Ayah-level edition differences

- **0** ayahs have a non-sajda word count difference.
- **15** ayahs (all sajda ayahs: 7:206, 13:15, 16:50, 17:109, 19:58, 22:18, 22:77, 25:60, 27:26, 32:15, 38:24, 41:38, 53:62, 84:21, 96:19) have `qcf4_words = tanzil_words + 1` because QCF4 stores an extra `#1969` sajda-decoration placeholder. Our `sajda` metadata table already records these; the placeholder is dropped at import time.

### Waqf-rule edge cases

| Case | Count | Rule outcome |
|---|---|---|
| Standalone waqf attached to preceding word in same ayah | 4 379 | Clean |
| Preceding mapped word is on a different QCF4 page than Tanzil's page for the waqf | 4 | Waqf keeps preceding word's line — falls on preceding QCF4 page. Documented; visually the pause mark stays with the phrase it terminates. |
| Waqf lies between two QCF4 lines (preceding on line N, following on line N+1) | 510 | Rule keeps waqf on line N (mushaf-standard behavior — pause marks attach to the word that ended a phrase). |
| Consecutive standalone-waqf streak (≥2 in a row) | 1 | Both attached to same preceding line. |
| Waqf at end of ayah, no next QCF4 word | 2 | Backward attach still resolves. |
| Waqf at end of ayah, no next Tanzil word | 17 | Backward attach still resolves. |
| Waqf at start of ayah with no preceding word (after excluding rub markers) | **0** | No exceptions. |
| Rub-marker ۞ forward-attached to next word | 199 | Clean. |
| Rub-marker ۞ with no following non-waqf word in ayah | **0** | No exceptions. |

### Crossing-page cases (fidelity item to acknowledge)

QCF4's 1441H layout and our current Tanzil page assignments (via quran-meta) disagree on which page some ayahs belong to. Measured:

- **56 ayahs** span a page boundary differently between the two editions.
- **305 word instances** (0.39 % of 77 433 non-waqf tokens).
- **25 distinct `(tanzil_page, qcf4_page)` pairs**. Most are ±1 (adjacent page). Concentrated in Al-Ma'idah (5), Ar-Rahman (55), and Juz Amma (surahs 78–114 → pages 582–604).

Complete list of affected ayahs and page pairs is in `docs/validation/qcf4-adoption/report.json`.

**Reconciliation decision required** — see "Consequences → open items" below.

## Consequences

### Positive

- Every Tanzil word gets a deterministic `(page, line)` — the renderer can be truly line-based (Charter #8).
- 100 % of Tanzil tokens map cleanly under 3 rules. No silent normalization. Zero real word deltas.
- Waqf placement matches printed-mushaf convention (pause marks stay with the phrase they end).
- MIT license on the JSON data — clean redistribution basis.
- No changes to Tanzil text, KFGQPC font, or the 604-page count.
- Font restriction on QCF4's per-page fonts is irrelevant to us — we don't ship them.

### Negative / accepted trade-offs

- **Horizontal line justification inside each line will differ from a physical 1441H mushaf**, because KFGQPC v2.2 has different glyph metrics than the QCF4 per-page PUA fonts the layout was calibrated for. Accepted: Decision 2 (2026-09-06) already chose Option (i) — QUL/QCF4 as line-assignment only, our font for rendering.
- **The `#1969` sajda-decoration placeholder is dropped**, so the printed page's inline sajda-decoration will not appear at that exact position in our render. Accepted: our `sajda` metadata table already records sajda ayahs; the Reader can render its own sajda indicator without needing QCF4's placeholder.
- **Edition mismatch is measured, not assumed:** 0 pages with word mismatches; 56 ayahs (305 word instances, 0.39 %) with page-boundary disagreement. These are real. See open item below.

### Open items (require product decision before implementation)

1. **Crossing-page ayahs (56 ayahs / 305 words) — pick one:**
   - **A. Trust QCF4's page assignments** — override `ayahs.page` for the 56 affected ayahs. Best fidelity to the printed 1441H mushaf structure. Requires touching `ayahs.page` (structural update to an existing column). Content SHA256 changes (already changing because of the two new tables).
   - **B. Keep our current Tanzil page assignments** — for crossing words, ignore QCF4's page and derive a fallback `(page, line)` using QCF4's line assignment within the Tanzil-assigned page (approximate). Line geometry becomes inconsistent for those 56 ayahs.
   - **C. Split the difference**: keep `ayahs.page` unchanged but override QCF4 line assignments for crossing ayahs (approximate, degrades line fidelity). Rejected as worst-of-both.
   - **Recommendation: A**. Cost: reassigning 56 ayahs' `page` column. Benefit: 100 % line-and-page fidelity to the printed 1441H mushaf across the whole dataset. Documenting the change in the pipeline validation report and the `sajda`/`ruku`/`juz` cross-references stay consistent (only `page` moves for those 56 ayahs; global_index doesn't).
2. **Waqf-between-lines convention (510 cases) — confirm rule:** "attach to preceding line" is the standard mushaf convention and what our validator already applies. Confirm.
3. **Attribution wording in `NOTICE`:** to be drafted alongside the MIT license text of QCF4.

### Follow-up work triggered by acceptance

- New Phase-1 pipeline stage `parse_qcf4_layout` reads the 604 JSONs from a vendored `data/sources/qcf4/repo/pages/` (git submodule or vendored copy pinned to commit `5130511027…`) and produces `page_lines.json` + `page_words.json`.
- New pipeline stage `reconcile_layout` applies the 3 mapping rules and enforces the invariants:
  - Total non-waqf mappings = Tanzil non-waqf token count.
  - Total waqf-rule mappings = Tanzil waqf token count.
  - Total rub-rule mappings = Tanzil ۞ token count.
  - Zero unmapped Tanzil tokens.
  - Zero unmapped QCF4 real words.
  - Exactly 15 unmapped QCF4 `#1969` sajda placeholders (dropped).
- New SQLite schema (additive):
  ```sql
  CREATE TABLE page_lines (
      page          INTEGER NOT NULL,
      line_index    INTEGER NOT NULL,
      line_type     TEXT NOT NULL CHECK(line_type IN ('ayah','surah_name','basmalah')),
      is_centered   INTEGER NOT NULL CHECK(is_centered IN (0,1)),
      surah_number  INTEGER,
      PRIMARY KEY(page, line_index)
  );
  CREATE TABLE page_words (
      page                 INTEGER NOT NULL,
      line_index           INTEGER NOT NULL,
      word_index_in_line   INTEGER NOT NULL,
      surah                INTEGER NOT NULL,
      ayah                 INTEGER NOT NULL,
      position_in_ayah     INTEGER NOT NULL,   -- Tanzil's position (includes waqf tokens)
      tanzil_token         TEXT NOT NULL,      -- authoritative token text
      token_kind           TEXT NOT NULL CHECK(token_kind IN ('word','waqf','rub')),
      PRIMARY KEY(page, line_index, word_index_in_line),
      FOREIGN KEY(page, line_index) REFERENCES page_lines(page, line_index),
      UNIQUE(surah, ayah, position_in_ayah)
  );
  ```
- `CONTENT_VERSION` → `1.1.0`; new `CONTENT_SHA256` regenerated and pinned in `android/core/database/src/main/java/app/mushaf/core/database/QuranAsset.kt`.
- `NOTICE` update: MIT license text for QCF4 data, attribution to Mohamad Hajj Rabee, and an explicit statement that QCF4 fonts are NOT redistributed.
- Reader (`ReaderScreen.kt`) reads from `page_lines` + `page_words` instead of concatenating `ayahs.text_uthmani`. If the two new tables are absent (dev builds against v1.0.0), the current text-flow renderer remains the fallback.

## References

- ADR-0021: Quran text source (Tanzil Uthmani).
- ADR-0022: pagination from `quran-center/quran-meta`.
- ADR-0024: font (KFGQPC Uthmanic Hafs v2.2).
- ADR-0017: text rendering.
- QCF4 source: <https://github.com/MohamadHajjRabee/quran-qcf4> commit `5130511027e769f0a8f4eeb7f00f46bde3788d60` (2026-08-11).
- QCF4 LICENSE.md: MIT for data, KFC-restricted for fonts (fonts not shipped).
- Validation artifacts (pre-migration PoC): `docs/validation/qcf4-adoption/report.md`, `docs/validation/qcf4-adoption/report.json`, `docs/validation/qcf4-adoption/per_page.csv`, `docs/validation/qcf4-adoption/per_ayah_deltas.csv`.
- Post-migration validator: `tools/post_migration_validate.py`. Last run 2026-09-07: **all 21 invariants pass**.

## Out of scope — permanent

**Tajweed (color-coded pronunciation rules) is OUT OF SCOPE.** Confirmed 2026-09-07.
The Reader renders monochrome mushaf text. Do not add tajweed rule detection, tajweed
data ingestion, tajweed color spans, tajweed legend, or tajweed database fields under
this ADR or a follow-up. The `TokenKind` enum is deliberately limited to `WORD`, `WAQF`,
and `RUB` — do not extend it with a tajweed variant. If a future product decision reverses
this, it must be captured in a new ADR that supersedes this note explicitly.

## Migration record (post-acceptance)

- Executed: 2026-09-07
- Content version: `1.0.0` → `1.1.0`
- Release DB SHA-256: `86e6be19da45d686146d4840b109b3ad0efb39525e366f080485589c4a2085cb`
- Release DB size: 9,224,192 bytes
- Ayah page overrides applied (QCF4 1441H authoritative): **56 ayahs** (0.9 %)
- Tanzil text integrity: byte-identical for all 6236 ayahs (pre/post SHA-256 hashes match)
- Sajda decoration placeholders dropped: 15 (documented, corresponds to the 15 sajda ayahs)
- Waqf reconciliation rule outcomes: 4 379 backward-attach; 0 failures
- Rub reconciliation rule outcomes: 199 forward-attach; 0 failures
- Files updated in shipping paths:
  - `data/pipeline/pipeline.py` (new stages parse_qcf4_layout, reconcile_layout, validate_layout; extended package_sqlite; CONTENT_VERSION 1.1.0)
  - `data/sources/qcf4/` (vendored pages + SOURCE.json, no fonts)
  - `data/output/RELEASE/quran-1.1.0.db` + `.sha256`
  - `android/app/src/main/assets/quran/quran.db`
  - `android/core/database/.../QuranAsset.kt` (version + hash)
  - `android/core/database/.../QuranDao.kt` (loadPageLines)
  - `android/core/domain/.../model/Page.kt` (added `lines`)
  - `android/core/domain/.../model/PageLine.kt` (new)
  - `android/feature/reader/.../ReaderScreen.kt` (line-by-line renderer with v1.0.0 fallback)
  - `LICENSES/quran-qcf4-MIT.txt`
  - `NOTICE`
