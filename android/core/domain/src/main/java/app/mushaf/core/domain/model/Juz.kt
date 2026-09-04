package app.mushaf.core.domain.model

data class Juz(
    val number: Int,                        // 1..30
    val firstAyahGlobalIndex: Int,
    val lastAyahGlobalIndex: Int,
    val firstSurah: Int,                    // Surah number containing the first ayah of this juz
    val firstAyahInSurah: Int,              // Ayah number within firstSurah
    val firstPageNumber: Int,               // 1..604
)
