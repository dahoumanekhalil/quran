package app.mushaf.core.domain.model

/**
 * One physical line of a Madani mushaf page.
 *
 * Per ADR-0026 the line-layout comes from QCF4 1441H; the words within
 * each line are the authoritative Tanzil tokens.
 */
data class PageLine(
    val lineIndex: Int,           // 1..15 (or 1..8 on the decorated Fatiha/Baqarah opening pages)
    val lineType: LineType,
    val isCentered: Boolean,
    val surahNumber: Int?,        // non-null for SURAH_NAME / BASMALAH lines
    val words: List<PageWord>,    // empty for SURAH_NAME / BASMALAH lines
)

enum class LineType { AYAH, SURAH_NAME, BASMALAH }

/**
 * One word (or waqf mark / rub marker) on a Mushaf line.
 *
 * `tanzilToken` is the exact byte-for-byte token from Tanzil `text_uthmani`.
 * QCF4 only supplied the (page, line, position) mapping — never the text.
 */
data class PageWord(
    val wordIndexInLine: Int,     // 1..N left-to-right in reading (RTL) order
    val surah: Int,
    val ayah: Int,
    val positionInAyah: Int,      // 1..M within the ayah's Tanzil tokenization
    val tanzilToken: String,
    val kind: TokenKind,
)

enum class TokenKind {
    /** A Tanzil word. */
    WORD,
    /** A standalone waqf / pause mark (U+06D6..U+06ED, excl. U+06DE) — backward-attached to preceding line. */
    WAQF,
    /** ۞ U+06DE Rub el-Hizb marker — forward-attached to next word's line. */
    RUB,
}
