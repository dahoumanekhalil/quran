# Quran Data Model (v1)

> Storage-independent schema for the Digital Mushaf's Quran dataset.
> Riwaya: **Hafs `an` Asim**. Pagination: **Madani mushaf, 15 lines per page, 604 pages**.

---

## Sources of truth

| Concern | Source | Version | License |
|---|---|---|---|
| Ayah text (Uthmani) | Tanzil Project | 1.1 (Feb 2021) | CC BY 3.0 (verbatim only) |
| Pagination, juz, hizb, rub, sajda, surah metadata | `quran-center/quran-meta` (`HafsLists.ts`) | 6.1.1-6, commit `a5dd4a4` | MIT |

Provenance manifests: `data/sources/tanzil-uthmani/1.1/SOURCE.json`, `data/sources/quran-meta/SOURCE.json`.

---

## Entities

### `Surah`

| Field | Type | Notes |
|---|---|---|
| `number` | int `1..114` | canonical Mushaf order |
| `name_ar` | string | Arabic name as printed in the Mushaf (e.g. `الفَاتِحة`) |
| `name_transliteration_en` | string | Latin transliteration (e.g. `Al-Fatihah`) |
| `revelation_place` | enum | `meccan` \| `medinan` |
| `revelation_order` | int `1..114` | chronological revelation order |
| `ayah_count` | int | total ayahs (Hafs count) |
| `ruku_count` | int | traditional ruku divisions within the surah |
| `bismillah_pre` | bool | `true` for all surahs except **9** (At-Tawbah); Al-Fatiha also counts its bismillah as its ayah 1 |
| `first_ayah_global_index` | int `1..6236` | index of this surah's first ayah in global ordering |

### `Ayah`

| Field | Type | Notes |
|---|---|---|
| `surah_number` | int `1..114` | |
| `ayah_number_in_surah` | int `1..N` | 1-indexed within surah |
| `ayah_global_index` | int `1..6236` | 1-indexed across the entire Quran (Hafs) |
| `text_uthmani` | string | **byte-preserving** import from Tanzil; no normalization, trim, replace, or NFC/NFD |
| `page_number` | int `1..604` | Madani 15-line mushaf page on which this ayah **starts** |
| `juz_number` | int `1..30` | |
| `hizb_number` | int `1..60` | |
| `rub_number` | int `1..240` | rub-al-hizb (quarter of hizb) |
| `is_sajda` | bool | `true` for the 15 sajda ayahs (see Sajda list) |
| `sajda_type` | enum? | `recommended` \| `obligatory` \| `null`. School-dependent — deferred to future ADR; MVP only stores `is_sajda`. |

### `Page`

| Field | Type | Notes |
|---|---|---|
| `number` | int `1..604` | |
| `first_ayah_global_index` | int | inclusive |
| `last_ayah_global_index` | int | inclusive |
| `surahs_on_page` | int[] | surah numbers appearing on this page, ordered |
| `juz_starts_on_page` | int[] | juz numbers whose first ayah is on this page |

### `Juz`

| Field | Type |
|---|---|
| `number` | int `1..30` |
| `first_ayah_global_index` | int |
| `last_ayah_global_index` | int |
| `first_surah_number` | int |
| `first_ayah_number_in_surah` | int |

### `Hizb`

| Field | Type |
|---|---|
| `number` | int `1..60` |
| `first_ayah_global_index` | int |
| `last_ayah_global_index` | int |

### `Rub` (rub-al-hizb, quarter of hizb)

| Field | Type |
|---|---|
| `number` | int `1..240` |
| `hizb_number` | int `1..60` |
| `quarter_in_hizb` | int `1..4` |
| `first_ayah_global_index` | int |
| `last_ayah_global_index` | int |

---

## Invariants (Hafs)

Numerical constants, sourced from `HafsMeta` in `HafsLists.ts` and cross-checked against Tanzil corpus counts:

| Constant | Value | Reference |
|---|---|---|
| Surahs | **114** | `HafsMeta.numSurahs` |
| Ayahs (total, Kufi count used by Hafs) | **6236** | `HafsMeta.numAyahs` |
| Pages (Madani 15-line mushaf) | **604** | `HafsMeta.numPages` |
| Juz | **30** | `HafsMeta.numJuzs` |
| Hizb | **60** | `HafsMeta.numHizbs` |
| Rub-al-hizb (quarters) | **240** | `HafsMeta.numRubAlHizbs` |
| Manzil | **7** | `HafsMeta.numManzils` |
| Ruku | **556** | `HafsMeta.numRukus` |
| Sajda | **15** | `HafsMeta.numSajdas` |
| Rubs per juz | **8** | `HafsMeta.numRubsInJuz` |

### Structural rules

1. Surahs numbered `1..114`, no gaps, unique.
2. Per-surah `ayah_count` sums to **6236**.
3. Global ayah indices are `1..6236`, unique, dense.
4. Every ayah references a valid page (`1..604`), juz (`1..30`), hizb (`1..60`), rub (`1..240`).
5. Every page has ≥ 1 ayah; every juz/hizb/rub starts at a documented ayah.
6. Every rub `r` belongs to hizb `ceil(r/4)`; `quarter_in_hizb(r) = ((r-1) % 4) + 1`.
7. Surah **9 (At-Tawbah)** has `bismillah_pre = false`; all others `true`.
8. `SajdaList` from `HafsLists.ts` has exactly **15** entries.
9. Ayah `1` (`surah_number=1, ayah_number_in_surah=1`) is Al-Fatiha's Bismillah (Hafs).

### Ownership rule for boundary ayahs

An ayah is assigned to exactly one page: the page on which the **ayah begins** (i.e., its first word appears). Visual continuation of an ayah's remaining words onto the next page is a rendering concern, not a data-model concern.

The same rule applies to juz, hizb, rub — assignment follows the ayah's starting word.

---

## Immutable content contract

- `text_uthmani` is imported **byte-for-byte** from the Tanzil source. The pipeline forbids `.trim()`, `.replace()`, Unicode normalization (NFC / NFD / NFKC / NFKD), and any character substitution.
- The corpus SHA-256 is recorded in the pipeline manifest and re-verified before packaging.
- Any change to text or metadata bumps the content version (see `TASK-024` — Content Integrity Gate) and requires re-running Phase 1 validation end-to-end.

---

## Storage mapping (informative)

Physical form for the Android app (produced in **TASK-022**) is a read-only SQLite file `quran.db` with tables `surahs`, `ayahs`, `pages`, `juz`, `hizb`, `rub`, `sajda`, plus an FTS5 virtual table over a normalized `search_text` column (built in **TASK-023**). The schema is a physical projection of this data model; this document is authoritative.
