package app.mushaf.core.data

import app.mushaf.core.database.QuranDao
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.Surah
import app.mushaf.core.domain.repository.QuranRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val dao: QuranDao,
) : QuranRepository {

    // Cache small immutable collections in memory — cheap and hit on almost every screen.
    @Volatile private var cachedSurahs: List<Surah>? = null
    @Volatile private var cachedJuz: List<Juz>? = null

    override suspend fun getAllSurahs(): List<Surah> =
        cachedSurahs ?: dao.allSurahs().also { cachedSurahs = it }

    override suspend fun getAllJuz(): List<Juz> =
        cachedJuz ?: dao.allJuz().also { cachedJuz = it }

    override suspend fun getPage(pageNumber: Int): Page {
        require(pageNumber in 1..604) { "Page $pageNumber out of range 1..604" }
        return dao.page(pageNumber)
    }

    override suspend fun getSurah(surahNumber: Int): Surah {
        require(surahNumber in 1..114) { "Surah $surahNumber out of range 1..114" }
        return getAllSurahs().first { it.number == surahNumber }
    }

    override suspend fun pageOfSurahStart(surahNumber: Int): Int {
        require(surahNumber in 1..114)
        return dao.pageOfSurahStart(surahNumber)
    }

    override suspend fun pageOfJuzStart(juzNumber: Int): Int {
        require(juzNumber in 1..30)
        return dao.pageOfJuzStart(juzNumber)
    }
}
