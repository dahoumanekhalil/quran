package app.mushaf.core.database

import android.database.sqlite.SQLiteDatabase
import app.mushaf.core.common.IoDispatcher
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.PageAyah
import app.mushaf.core.domain.model.RevelationPlace
import app.mushaf.core.domain.model.Surah
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranDao @Inject constructor(
    private val db: QuranDb,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val sqlite: SQLiteDatabase get() = db.database

    suspend fun allSurahs(): List<Surah> = withContext(io) {
        val out = ArrayList<Surah>(114)
        sqlite.rawQuery(
            """
            SELECT number, name_ar, name_translit_en, name_translation_en,
                   revelation_order, revelation_place, ayah_count, ruku_count,
                   bismillah_pre, first_ayah_global_index
            FROM surahs ORDER BY number
            """.trimIndent(),
            null,
        ).use { c ->
            while (c.moveToNext()) {
                out += Surah(
                    number = c.getInt(0),
                    nameAr = c.getString(1),
                    nameTranslitEn = c.getString(2),
                    nameTranslationEn = c.getString(3),
                    revelationOrder = c.getInt(4),
                    revelationPlace = if (c.getString(5) == "meccan") RevelationPlace.MECCAN else RevelationPlace.MEDINAN,
                    ayahCount = c.getInt(6),
                    rukuCount = c.getInt(7),
                    bismillahPre = c.getInt(8) == 1,
                    firstAyahGlobalIndex = c.getInt(9),
                )
            }
        }
        out
    }

    suspend fun surah(surahNumber: Int): Surah = withContext(io) {
        sqlite.rawQuery(
            """
            SELECT number, name_ar, name_translit_en, name_translation_en,
                   revelation_order, revelation_place, ayah_count, ruku_count,
                   bismillah_pre, first_ayah_global_index
            FROM surahs WHERE number = ? LIMIT 1
            """.trimIndent(),
            arrayOf(surahNumber.toString()),
        ).use { c ->
            check(c.moveToNext()) { "Surah $surahNumber not found" }
            Surah(
                number = c.getInt(0),
                nameAr = c.getString(1),
                nameTranslitEn = c.getString(2),
                nameTranslationEn = c.getString(3),
                revelationOrder = c.getInt(4),
                revelationPlace = if (c.getString(5) == "meccan") RevelationPlace.MECCAN else RevelationPlace.MEDINAN,
                ayahCount = c.getInt(6),
                rukuCount = c.getInt(7),
                bismillahPre = c.getInt(8) == 1,
                firstAyahGlobalIndex = c.getInt(9),
            )
        }
    }

    suspend fun allJuz(): List<Juz> = withContext(io) {
        val out = ArrayList<Juz>(30)
        sqlite.rawQuery(
            """
            SELECT j.number, j.first_ayah_global_index, j.last_ayah_global_index,
                   a.surah, a.ayah, a.page
            FROM juz j JOIN ayahs a ON a.global_index = j.first_ayah_global_index
            ORDER BY j.number
            """.trimIndent(),
            null,
        ).use { c ->
            while (c.moveToNext()) {
                out += Juz(
                    number = c.getInt(0),
                    firstAyahGlobalIndex = c.getInt(1),
                    lastAyahGlobalIndex = c.getInt(2),
                    firstSurah = c.getInt(3),
                    firstAyahInSurah = c.getInt(4),
                    firstPageNumber = c.getInt(5),
                )
            }
        }
        out
    }

    suspend fun page(pageNumber: Int): Page = withContext(io) {
        val pageMeta = sqlite.rawQuery(
            """
            SELECT number, first_ayah_global_index, last_ayah_global_index,
                   surahs_on_page_json, juz_starts_on_page_json
            FROM pages WHERE number = ? LIMIT 1
            """.trimIndent(),
            arrayOf(pageNumber.toString()),
        ).use { c ->
            check(c.moveToNext()) { "Page $pageNumber not found" }
            PageMeta(
                number = c.getInt(0),
                firstGid = c.getInt(1),
                lastGid = c.getInt(2),
                surahsOnPage = parseIntArray(c.getString(3)),
                juzStartsOnPage = parseIntArray(c.getString(4)),
            )
        }
        val ayahs = ArrayList<PageAyah>()
        val seenSurah = HashSet<Int>()
        sqlite.rawQuery(
            """
            SELECT a.global_index, a.surah, a.ayah, a.text_uthmani, a.is_sajda,
                   s.name_ar, s.bismillah_pre
            FROM ayahs a JOIN surahs s ON s.number = a.surah
            WHERE a.page = ?
            ORDER BY a.global_index
            """.trimIndent(),
            arrayOf(pageNumber.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                val surahNum = c.getInt(1)
                val ayahNum = c.getInt(2)
                val startsHere = (surahNum !in seenSurah) && ayahNum == 1
                seenSurah += surahNum
                ayahs += PageAyah(
                    globalIndex = c.getInt(0),
                    surah = surahNum,
                    ayah = ayahNum,
                    textUthmani = c.getString(3),
                    isSajda = c.getInt(4) == 1,
                    surahNameAr = c.getString(5),
                    surahStartsHere = startsHere,
                    bismillahPre = c.getInt(6) == 1,
                )
            }
        }
        Page(
            number = pageMeta.number,
            firstAyahGlobalIndex = pageMeta.firstGid,
            lastAyahGlobalIndex = pageMeta.lastGid,
            surahsOnPage = pageMeta.surahsOnPage,
            juzStartsOnPage = pageMeta.juzStartsOnPage,
            ayahs = ayahs,
        )
    }

    suspend fun pageOfSurahStart(surahNumber: Int): Int = withContext(io) {
        sqlite.rawQuery(
            """
            SELECT a.page FROM ayahs a JOIN surahs s ON s.number = a.surah
            WHERE a.surah = ? AND a.ayah = 1 LIMIT 1
            """.trimIndent(),
            arrayOf(surahNumber.toString()),
        ).use { c ->
            check(c.moveToNext()) { "Page for surah $surahNumber start not found" }
            c.getInt(0)
        }
    }

    suspend fun pageOfJuzStart(juzNumber: Int): Int = withContext(io) {
        sqlite.rawQuery(
            """
            SELECT a.page FROM juz j JOIN ayahs a ON a.global_index = j.first_ayah_global_index
            WHERE j.number = ? LIMIT 1
            """.trimIndent(),
            arrayOf(juzNumber.toString()),
        ).use { c ->
            check(c.moveToNext()) { "Page for juz $juzNumber start not found" }
            c.getInt(0)
        }
    }

    private data class PageMeta(
        val number: Int,
        val firstGid: Int,
        val lastGid: Int,
        val surahsOnPage: List<Int>,
        val juzStartsOnPage: List<Int>,
    )

    private fun parseIntArray(json: String): List<Int> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (trimmed.isEmpty()) return emptyList()
        return trimmed.split(',').map { it.trim().toInt() }
    }
}
