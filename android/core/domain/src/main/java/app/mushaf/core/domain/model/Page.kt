package app.mushaf.core.domain.model

/**
 * One page of the Madani 604-page mushaf.
 *
 * `ayahs` covers every ayah whose starting word appears on this page
 * (in reading order). Visual continuation of an ayah's last word onto
 * the next page is a rendering concern, not part of this data model.
 */
data class Page(
    val number: Int,                      // 1..604
    val firstAyahGlobalIndex: Int,
    val lastAyahGlobalIndex: Int,
    val surahsOnPage: List<Int>,          // surah numbers, ordered
    val juzStartsOnPage: List<Int>,       // juz numbers whose first ayah is on this page
    val ayahs: List<PageAyah>,
    /**
     * Physical mushaf lines (QCF4 1441H layout, ADR-0026). Ordered by lineIndex.
     * Empty for pages that predate the line-layout migration (v1.0.0 asset).
     */
    val lines: List<PageLine> = emptyList(),
)

/** Lightweight ayah row used by the Reader — no juz/rub metadata needed for rendering. */
data class PageAyah(
    val globalIndex: Int,
    val surah: Int,
    val ayah: Int,
    val textUthmani: String,
    val surahNameAr: String,
    val surahStartsHere: Boolean,   // ayah == 1 && this is the first ayah of the surah on this page
    val bismillahPre: Boolean,      // true for all surahs except 9
    val isSajda: Boolean,
)
