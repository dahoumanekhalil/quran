package app.mushaf.core.data

import app.mushaf.core.database.QuranDao
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.RevelationPlace
import app.mushaf.core.domain.model.Surah
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class QuranRepositoryImplTest {

    private val dao: QuranDao = mockk()

    private fun surah(n: Int) = Surah(
        number = n,
        nameAr = "س$n",
        nameTranslitEn = "S$n",
        nameTranslationEn = "S$n",
        revelationPlace = RevelationPlace.MECCAN,
        revelationOrder = n,
        ayahCount = 7,
        rukuCount = 1,
        bismillahPre = n != 9,
        firstAyahGlobalIndex = 1,
    )

    private fun juz(n: Int) = Juz(
        number = n,
        firstAyahGlobalIndex = 1,
        lastAyahGlobalIndex = 200,
        firstSurah = 1,
        firstAyahInSurah = 1,
        firstPageNumber = 1,
    )

    @Test
    fun getAllSurahsCachesResult() = runTest {
        val list = (1..114).map(::surah)
        coEvery { dao.allSurahs() } returns list
        val repo = QuranRepositoryImpl(dao)

        repo.getAllSurahs()
        repo.getAllSurahs()
        repo.getAllSurahs()

        coVerify(exactly = 1) { dao.allSurahs() }
    }

    @Test
    fun getAllJuzCachesResult() = runTest {
        coEvery { dao.allJuz() } returns (1..30).map(::juz)
        val repo = QuranRepositoryImpl(dao)

        repo.getAllJuz()
        repo.getAllJuz()

        coVerify(exactly = 1) { dao.allJuz() }
    }

    @Test
    fun getPageRejectsOutOfRange() {
        val repo = QuranRepositoryImpl(dao)

        assertThrows<IllegalArgumentException> { runBlocking { repo.getPage(0) } }
        assertThrows<IllegalArgumentException> { runBlocking { repo.getPage(605) } }
    }

    @Test
    fun getPageDelegatesToDao() = runTest {
        val page = Page(
            number = 42,
            firstAyahGlobalIndex = 1000,
            lastAyahGlobalIndex = 1010,
            surahsOnPage = listOf(2),
            juzStartsOnPage = emptyList(),
            ayahs = emptyList(),
        )
        coEvery { dao.page(42) } returns page
        val repo = QuranRepositoryImpl(dao)

        val result = repo.getPage(42)

        assertThat(result).isEqualTo(page)
    }

    @Test
    fun getSurahLooksUpFromCache() = runTest {
        coEvery { dao.allSurahs() } returns (1..114).map(::surah)
        val repo = QuranRepositoryImpl(dao)

        val result = repo.getSurah(9)

        assertThat(result.number).isEqualTo(9)
        assertThat(result.bismillahPre).isFalse()
    }

    @Test
    fun getSurahRejectsOutOfRange() {
        coEvery { dao.allSurahs() } returns (1..114).map(::surah)
        val repo = QuranRepositoryImpl(dao)

        assertThrows<IllegalArgumentException> { runBlocking { repo.getSurah(0) } }
        assertThrows<IllegalArgumentException> { runBlocking { repo.getSurah(115) } }
    }
}
