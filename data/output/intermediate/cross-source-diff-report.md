# Cross-source diff report — Tanzil vs Quran.com (KFGQPC)

**Purpose:** independent verification that our import pipeline preserves the
Quran text intact. We compare Tanzil Uthmani (source A, our primary) against
Quran.com's Uthmani text (source B, derived from the King Fahd Quran Printing
Complex — an independent lineage). Diacritic-level differences between
the two traditions are **expected** and are not a defect; structural
differences (missing ayahs, wrong ordering, skeleton mismatches on the
consonantal text) would indicate a defect and block release.

## Sources
- **A:** Tanzil Project Uthmani 1.1 (data/sources/tanzil-uthmani/1.1/quran-uthmani.xml)
- **B:** Quran.com API v4 Uthmani (data/sources/quran-com/v4/verses-uthmani.json) — KFGQPC-derived

## Counts

| Metric | Count |
|---|---:|
| Ayahs in A | 6236 |
| Ayahs in B | 6236 |
| Common keys | 6236 |
| Only in A | 0 |
| Only in B | 0 |
| Byte-identical (of common) | 6126 |
| **Skeleton-equal (of common)** | **6236** |
| Skeleton diffs | 0 |

**Skeleton equality** = both texts, after removing all diacritics and Quranic
annotation marks and after normalizing alef/ya/ta-marbuta variants and
collapsing whitespace, are byte-identical.

## Verdict: PASS

