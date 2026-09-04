package app.mushaf.core.domain.model

/** Pure-domain Surah. No Android imports. */
data class Surah(
    val number: Int,               // 1..114
    val nameAr: String,
    val nameTranslitEn: String,
    val nameTranslationEn: String,
    val revelationPlace: RevelationPlace,
    val revelationOrder: Int,      // 1..114
    val ayahCount: Int,
    val rukuCount: Int,
    val bismillahPre: Boolean,     // false only for surah 9
    val firstAyahGlobalIndex: Int, // 1..6236
)

enum class RevelationPlace { MECCAN, MEDINAN }
