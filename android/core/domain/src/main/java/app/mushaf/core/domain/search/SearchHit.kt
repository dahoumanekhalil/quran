package app.mushaf.core.domain.search

import app.mushaf.core.domain.model.Ayah

/**
 * One row of Quran search output.
 *
 * `snippet` is the original diacritized Uthmani text (never the normalized
 * skeleton) so the reader can display it faithfully.
 *
 * `matchPositions` is left empty by MVP — inline highlight of matched substrings
 * is deferred to POST-MVP; the search result cards still show the whole ayah.
 *
 * `rank` is the raw FTS5 bm25 score (lower is better); useful for debugging /
 * telemetry but not shown to the user.
 */
data class SearchHit(
    val ayah: Ayah,
    val surahNameAr: String,
    val snippet: String,
    val matchPositions: List<IntRange> = emptyList(),
    val rank: Double = 0.0,
)
