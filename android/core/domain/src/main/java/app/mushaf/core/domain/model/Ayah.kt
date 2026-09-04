package app.mushaf.core.domain.model

/**
 * A single ayah with the Uthmani text preserved byte-for-byte from the
 * Tanzil source. `text_uthmani` MUST NOT be normalized, trimmed, or
 * replaced anywhere in the app.
 */
data class Ayah(
    val globalIndex: Int,   // 1..6236
    val surah: Int,         // 1..114
    val ayah: Int,          // 1..N within surah
    val textUthmani: String,
    val page: Int,          // 1..604
    val juz: Int,           // 1..30
    val hizb: Int,          // 1..60
    val rub: Int,           // 1..240
    val quarterInHizb: Int, // 1..4
    val manzil: Int,        // 1..7
    val ruku: Int,          // 1..556
    val isSajda: Boolean,
)
