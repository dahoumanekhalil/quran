package app.mushaf.core.domain.repository

import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.Surah

/**
 * Read-only access to the frozen Quran content.
 * Implementations live in :core:data; underlying storage is the bundled
 * SQLite asset per ADR-0007 / ADR-0014.
 */
interface QuranRepository {

    /** All 114 surahs, ordered by surah number. Cached by the implementation. */
    suspend fun getAllSurahs(): List<Surah>

    /** All 30 juz, ordered by juz number. */
    suspend fun getAllJuz(): List<Juz>

    /** A single page (1..604) with all ayahs whose starting word is on that page. */
    suspend fun getPage(pageNumber: Int): Page

    /** Convenience — surah metadata by number. */
    suspend fun getSurah(surahNumber: Int): Surah

    /** Convenience — page number containing the first ayah of a surah. */
    suspend fun pageOfSurahStart(surahNumber: Int): Int

    /** Convenience — page number containing the first ayah of a juz. */
    suspend fun pageOfJuzStart(juzNumber: Int): Int
}
