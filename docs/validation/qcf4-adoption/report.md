# QCF4 Layout ↔ Tanzil Text — Full 604-page Validation Report

## Sources
- **QCF4 (candidate layout)**: `MohamadHajjRabee/quran-qcf4`
  - commit `5130511027e769f0a8f4eeb7f00f46bde3788d60`
  - commit date `2026-08-11`
  - license (data): MIT
  - license (fonts): KFC EULA — NOT redistributed by us
- **Tanzil (authoritative text)**: content v1.0.0 (frozen), Tanzil Uthmani v1.1

## Alignment strategy

Per-ayah sequential walk. QCF4's `position_in_ayah` numbers non-waqf words only (waqf marks are bundled into the preceding word). Tanzil numbers include standalone waqf tokens. Therefore `(surah, ayah, position)` is NOT a shared key. We instead:

1. For each ayah, list Tanzil tokens in order and QCF4 word entries in reading order.
2. Walk Tanzil tokens: for each non-waqf token, consume the next QCF4 entry sequentially and record its `(page, line)`.
3. For each waqf-only Tanzil token, apply the candidate rule: attach to the last mapped non-waqf word's `(page, line)`.
4. Record any residual Tanzil non-waqf tokens (edition delta: Tanzil longer) and any residual QCF4 entries (edition delta: QCF4 longer).

No silent normalization. QCF4 text values are not used for matching — only for reporting.

## Totals

| Metric | Value |
|---|---|
| Pages checked | 604 |
| QCF4 total lines | 9046 |
| QCF4 word entries | 77448 |
| Tanzil total tokens | 82011 |
| Tanzil non-waqf tokens | 77433 |
| Tanzil waqf-only tokens | 4578 |
| Sequential mappings (non-waqf) | **77433** |
| Waqf-rule mappings (backward attach) | **4379** |
| Rub-marker mappings (forward attach) | **199** |
| Unmapped Tanzil non-waqf | **0** |
| Unmapped Tanzil waqf after rule | **0** |
| Unmapped Tanzil rub after rule | **0** |
| Unmapped QCF4 real words (edition delta) | **0** |
| Unmapped QCF4 sajda-marker placeholders | **15** |
| Tanzil words whose QCF4 page differs from Tanzil page | **305** |

## Per-page distribution

| Metric | Pages |
|---|---|
| Pages with any count difference | 15 / 604 |
| Pages requiring waqf reconciliation | 587 / 604 |
| Pages with edition-difference (non-waqf) | 0 / 604 |
| Max affected words on any single page | **20** (page 35) |

Ayahs where Tanzil non-waqf count ≠ QCF4 count: **15** / 6236

## Waqf rule edge cases

| Case | Count |
|---|---|
| attached_to_preceding_same_ayah | 4379 |
| waqf_at_ayah_start_no_preceding | 0 |
| waqf_preceding_mapped_on_different_page | 4 |
| waqf_preceding_mapped_but_next_word_on_next_line | 510 |
| waqf_consecutive_streak_of_2_or_more | 1 |
| waqf_at_end_of_ayah_no_next_qcf4_word | 2 |
| waqf_at_end_of_ayah_no_next_tanzil_word | 17 |
| rub_forward_attached | 199 |
| rub_no_following_word | 0 |

**Waqf rule succeeded for every standalone waqf token that had a preceding mapped word.**

### Unmapped Tanzil non-waqf tokens (real edition delta, Tanzil longer) (first 0 of 0)

_none_

### Unmapped Tanzil waqf after rule (first 0 of 0)

_none_

### Unmapped Tanzil rub-marker after rule (first 0 of 0)

_none_

### Unmapped QCF4 REAL words (real edition delta, QCF4 longer) (first 0 of 0)

_none_

### Unmapped QCF4 sajda-marker placeholders (#1969 — known QCF4 decoration) (first 15 of 15)

- `{'surah': 7, 'ayah': 206, 'qcf4_position': 12, 'qcf4_page': 176, 'qcf4_line': 15, 'qcf4_text': '#1969'}`
- `{'surah': 13, 'ayah': 15, 'qcf4_position': 12, 'qcf4_page': 251, 'qcf4_line': 4, 'qcf4_text': '#1969'}`
- `{'surah': 16, 'ayah': 50, 'qcf4_position': 8, 'qcf4_page': 272, 'qcf4_line': 11, 'qcf4_text': '#1969'}`
- `{'surah': 17, 'ayah': 109, 'qcf4_position': 6, 'qcf4_page': 293, 'qcf4_line': 6, 'qcf4_text': '#1969'}`
- `{'surah': 19, 'ayah': 58, 'qcf4_position': 30, 'qcf4_page': 309, 'qcf4_line': 8, 'qcf4_text': '#1969'}`
- `{'surah': 22, 'ayah': 18, 'qcf4_position': 38, 'qcf4_page': 334, 'qcf4_line': 8, 'qcf4_text': '#1969'}`
- `{'surah': 22, 'ayah': 77, 'qcf4_position': 12, 'qcf4_page': 341, 'qcf4_line': 9, 'qcf4_text': '#1969'}`
- `{'surah': 25, 'ayah': 60, 'qcf4_position': 14, 'qcf4_page': 365, 'qcf4_line': 7, 'qcf4_text': '#1969'}`
- `{'surah': 27, 'ayah': 26, 'qcf4_position': 9, 'qcf4_page': 379, 'qcf4_line': 6, 'qcf4_text': '#1969'}`
- `{'surah': 32, 'ayah': 15, 'qcf4_position': 16, 'qcf4_page': 416, 'qcf4_line': 8, 'qcf4_text': '#1969'}`
- `{'surah': 38, 'ayah': 24, 'qcf4_position': 33, 'qcf4_page': 454, 'qcf4_line': 11, 'qcf4_text': '#1969'}`
- `{'surah': 41, 'ayah': 38, 'qcf4_position': 13, 'qcf4_page': 480, 'qcf4_line': 15, 'qcf4_text': '#1969'}`
- `{'surah': 53, 'ayah': 62, 'qcf4_position': 4, 'qcf4_page': 528, 'qcf4_line': 9, 'qcf4_text': '#1969'}`
- `{'surah': 84, 'ayah': 21, 'qcf4_position': 7, 'qcf4_page': 589, 'qcf4_line': 14, 'qcf4_text': '#1969'}`
- `{'surah': 96, 'ayah': 19, 'qcf4_position': 6, 'qcf4_page': 598, 'qcf4_line': 3, 'qcf4_text': '#1969'}`

### Crossing-page cases (Tanzil page != QCF4 page for aligned token) (first 20 of 305)

- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 1, 'token': 'قُلْ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 13}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 2, 'token': 'يَـٰٓأَهْلَ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 3, 'token': 'ٱلْكِتَـٰبِ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 4, 'token': 'لَا', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 5, 'token': 'تَغْلُوا۟', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 6, 'token': 'فِى', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 7, 'token': 'دِينِكُمْ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 8, 'token': 'غَيْرَ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 9, 'token': 'ٱلْحَقِّ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 10, 'token': 'وَلَا', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 11, 'token': 'تَتَّبِعُوٓا۟', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 12, 'token': 'أَهْوَآءَ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 14}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 13, 'token': 'قَوْمٍ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 14, 'token': 'قَدْ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 15, 'token': 'ضَلُّوا۟', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 16, 'token': 'مِن', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 17, 'token': 'قَبْلُ', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 18, 'token': 'وَأَضَلُّوا۟', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 19, 'token': 'كَثِيرًا', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`
- `{'surah': 5, 'ayah': 77, 'position_in_ayah': 20, 'token': 'وَضَلُّوا۟', 'tanzil_page': 121, 'qcf4_page': 120, 'qcf4_line': 15}`

## First per-ayah non-waqf count deltas

| surah:ayah | tanzil non-waqf | qcf4 | delta |
|---|---|---|---|
| 7:206 | 11 | 12 | -1 |
| 13:15 | 11 | 12 | -1 |
| 16:50 | 7 | 8 | -1 |
| 17:109 | 5 | 6 | -1 |
| 19:58 | 29 | 30 | -1 |
| 22:18 | 37 | 38 | -1 |
| 22:77 | 11 | 12 | -1 |
| 25:60 | 13 | 14 | -1 |
| 27:26 | 8 | 9 | -1 |
| 32:15 | 15 | 16 | -1 |
| 38:24 | 32 | 33 | -1 |
| 41:38 | 12 | 13 | -1 |
| 53:62 | 3 | 4 | -1 |
| 84:21 | 6 | 7 | -1 |
| 96:19 | 5 | 6 | -1 |

## Verdict (measured)

- Fully-aligned pages (no waqf rule needed, no edition delta): **589 / 604**
- Pages needing waqf rule only: **587 / 604**
- Pages with edition delta (non-waqf): **0 / 604**
- Words affected by waqf rule: **4379 / 82011** (5.34%)
- Tanzil non-waqf words unmapped: **0** (0.0000% of non-waqf)
- QCF4 real words unmapped (edition delta): **0** (0.0000% of QCF4)
- QCF4 sajda-marker placeholders (known decoration, safely ignored): **15**
- Tanzil words routed to a different QCF4 page than Tanzil's own page: **305** (0.3939% of non-waqf)