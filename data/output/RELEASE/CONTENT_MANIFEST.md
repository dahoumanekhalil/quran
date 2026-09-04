# CONTENT MANIFEST — Digital Mushaf Quran dataset v1.0.0

- **Frozen at:** 2026-09-04T21:20:40Z
- **Release DB:** `data/output/RELEASE/quran-1.0.0.db`
- **SHA-256:** `0ba78b6ab99f57a5688adc572f35ccf0568716a48d4424378d73cfd91c0b093e`
- **Size:** 3,260,416 bytes
- **Schema:** v1.0.0 (see `docs/data-model.md`)

## Riwaya and pagination
- Riwaya: Hafs `an` Asim
- Pagination: Madani mushaf, 15 lines per page, **604 pages**

## Sources

### Quran text — Tanzil Project (primary)
- Version: 1.1 (February 2021)
- License: CC BY 3.0 (verbatim copies only; text may not be modified)
- Fetched: 2026-09-04T21:03:56Z
- Files:
  - `quran-uthmani.xml` — SHA-256 `c5052534d63d3856ce25413ff464d0b609f90169a202a1615aa8707efec81244` (1,541,984 B)
  - `quran-uthmani.txt` — SHA-256 `7f30c647331a61100ebf24a80507dc0fcdd9f2df97f1312b5b2dfcb982a7f326` (1,396,684 B)
  - `quran-uthmani-min.xml` — SHA-256 `55e418f56115757e05262edbe57c80de08d176deda57a35870efc928c8ccd6cc` (1,343,922 B)

### Pagination + metadata — quran-center/quran-meta
- Package version: 6.1.1-6
- Git commit: `a5dd4a46dc6f7830a4303e89c3b4b3a15a213ac9`
- License: MIT
- Fetched: 2026-09-04T21:05:14Z
- Files consumed:
  - `src/lists/HafsLists.ts` — SHA-256 `49ddb293c92c8fb8d15ffe7ca09f5dd4bfedb8eb58c777e826525d867557d616` (15,455 B)
  - `src/lists/types.ts` — SHA-256 `62e1c763496d943bbb5ccbab8b46e7c579a94d65828757eb59ff3290faf8b51b` (1,977 B)
  - `src/types.ts` — SHA-256 `92a036d97a746d3d2317eb1b18268c04e6597f5f2f376f4f8b4d91ae41215dac` (8,974 B)
  - `src/i18n/surah.en.ts` — SHA-256 `07a25b0250376e6f03cc4d666078d7d53714aaf16a3ec5779ece5803b19a6882` (3,911 B)
  - `LICENSE` — SHA-256 `bae543d395b45f4ee9405e4c5acfa7b479d08ec756bde3ca2c41d0a59bf2f672` (1,090 B)
  - `package.json` — SHA-256 `242a7a9c1cab6c205ec8b6bb23557c19a9846b9827f92e8331e83294768dcb13` (3,925 B)

### Cross-source verification — Quran.com API v4 (independent, KFGQPC-derived)
- Endpoint: https://api.quran.com/api/v4/quran/verses/uthmani
- License: per quran.com/api — see https://quran.com/api
- Fetched: 2026-09-04T21:20:19Z
  - `v4/verses-uthmani.json` — SHA-256 `3754c592dd15d7047d5b4339737ad3171c5c1d431a8c3e4c1eee7781c135d58c` (1,654,246 B)

## Invariants

| Metric | Value |
|---|---:|
| ayahs | 6236 |
| hizb | 60 |
| juz | 30 |
| manzil | 7 |
| pages | 604 |
| rub | 240 |
| ruku | 556 |
| sajda | 15 |
| surahs | 114 |

## Validation summary

- Structural: **PASS** (`data/output/intermediate/validate-structural.json`)
- Unicode: **PASS** (`data/output/intermediate/validate-unicode.json`, profile: `unicode-profile.md`)
- Cross-source (skeleton): **6236/6236 skeleton-equal** — PASS (`data/output/intermediate/cross-source-diff-report.md`)
- Search index: **PASS** — 9 canonical queries executed

## Immutable content contract

- `text_uthmani` is imported byte-for-byte from Tanzil (no trim/replace/normalization).
- Corpus SHA-256 (LF-joined ayah text): tracked in `data/output/intermediate/tanzil-corpus.sha256`.
- text_uthmani SHA-256 (pre/post search-index build): `3a802babd61fd0835121feea6c054e16df4809417968e1f7b58aa18cdbae6e66` — identical before and after.

## Change policy

Any change to text or metadata bumps the content version and requires a full
Phase 1 re-run. Git tag `content-v1.0.0` will be applied when
the repository is initialized.

