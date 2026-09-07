package app.mushaf.core.database

import app.mushaf.core.common.IoDispatcher
import app.mushaf.core.domain.model.Ayah
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.LineType
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.PageAyah
import app.mushaf.core.domain.model.PageLine
import app.mushaf.core.domain.model.PageWord
import app.mushaf.core.domain.model.RevelationPlace
import app.mushaf.core.domain.model.Surah
import app.mushaf.core.domain.model.TokenKind
import app.mushaf.core.domain.search.SearchHit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranDao @Inject constructor(
    private val db: QuranDb,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    suspend fun allSurahs(): List<Surah> = withContext(io) {
        val sqlite = db.database()
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
        val sqlite = db.database()
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
        val sqlite = db.database()
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
        val sqlite = db.database()
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
        val lines = loadPageLines(sqlite, pageNumber)
        Page(
            number = pageMeta.number,
            firstAyahGlobalIndex = pageMeta.firstGid,
            lastAyahGlobalIndex = pageMeta.lastGid,
            surahsOnPage = pageMeta.surahsOnPage,
            juzStartsOnPage = pageMeta.juzStartsOnPage,
            ayahs = ayahs,
            lines = lines,
        )
    }

    /**
     * Load QCF4 line layout for the page (ADR-0026). Returns empty if the bundled
     * asset predates the layout migration — callers should fall back to the
     * ayah-flow renderer in that case.
     */
    private fun loadPageLines(sqlite: android.database.sqlite.SQLiteDatabase, pageNumber: Int): List<PageLine> {
        val hasTable = sqlite.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='page_lines' LIMIT 1",
            null,
        ).use { it.moveToFirst() }
        if (!hasTable) return emptyList()

        // 1) line rows
        data class LineRow(val idx: Int, val type: LineType, val centered: Boolean, val surahNumber: Int?)
        val lineRows = ArrayList<LineRow>(15)
        sqlite.rawQuery(
            """
            SELECT line_index, line_type, is_centered, surah_number
            FROM page_lines
            WHERE page = ?
            ORDER BY line_index
            """.trimIndent(),
            arrayOf(pageNumber.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                val t = when (c.getString(1)) {
                    "ayah" -> LineType.AYAH
                    "surah_name" -> LineType.SURAH_NAME
                    "basmalah" -> LineType.BASMALAH
                    else -> LineType.AYAH
                }
                lineRows += LineRow(
                    idx = c.getInt(0),
                    type = t,
                    centered = c.getInt(2) == 1,
                    surahNumber = if (c.isNull(3)) null else c.getInt(3),
                )
            }
        }
        if (lineRows.isEmpty()) return emptyList()

        // 2) word rows grouped by line
        val wordsByLine = HashMap<Int, MutableList<PageWord>>(lineRows.size)
        sqlite.rawQuery(
            """
            SELECT line_index, word_index_in_line, surah, ayah, position_in_ayah, tanzil_token, token_kind
            FROM page_words
            WHERE page = ?
            ORDER BY line_index, word_index_in_line
            """.trimIndent(),
            arrayOf(pageNumber.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                val kind = when (c.getString(6)) {
                    "word" -> TokenKind.WORD
                    "waqf" -> TokenKind.WAQF
                    "rub" -> TokenKind.RUB
                    else -> TokenKind.WORD
                }
                val li = c.getInt(0)
                val list = wordsByLine.getOrPut(li) { ArrayList(10) }
                list += PageWord(
                    wordIndexInLine = c.getInt(1),
                    surah = c.getInt(2),
                    ayah = c.getInt(3),
                    positionInAyah = c.getInt(4),
                    tanzilToken = c.getString(5),
                    kind = kind,
                )
            }
        }

        return lineRows.map { lr ->
            PageLine(
                lineIndex = lr.idx,
                lineType = lr.type,
                isCentered = lr.centered,
                surahNumber = lr.surahNumber,
                words = wordsByLine[lr.idx] ?: emptyList(),
            )
        }
    }

    suspend fun pageOfSurahStart(surahNumber: Int): Int = withContext(io) {
        val sqlite = db.database()
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
        val sqlite = db.database()
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

    /**
     * FTS5 skeleton-match search. `normalizedQuery` must already be run through
     * `SearchNormalizer.normalize()` — the client-side normalizer mirrors the
     * pipeline-side normalization that populated `ayahs.search_text`.
     *
     * Ranking: bm25 ascending (smaller = better), tie-broken by surah then
     * ayah for a stable, mushaf-ordered listing.
     */
    suspend fun search(normalizedQuery: String, limit: Int): List<SearchHit> = withContext(io) {
        val sqlite = db.database()
        val out = ArrayList<SearchHit>(limit.coerceAtMost(200))
        sqlite.rawQuery(
            """
            SELECT a.global_index, a.surah, a.ayah, a.text_uthmani, a.page,
                   a.juz, a.hizb, a.rub, a.quarter_in_hizb, a.manzil, a.ruku, a.is_sajda,
                   s.name_ar, bm25(ayahs_fts) AS rank
            FROM ayahs_fts
            JOIN ayahs a ON a.global_index = ayahs_fts.rowid
            JOIN surahs s ON s.number = a.surah
            WHERE ayahs_fts MATCH ?
            ORDER BY rank, a.surah, a.ayah
            LIMIT ?
            """.trimIndent(),
            arrayOf(normalizedQuery, limit.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                val ayah = Ayah(
                    globalIndex = c.getInt(0),
                    surah = c.getInt(1),
                    ayah = c.getInt(2),
                    textUthmani = c.getString(3),
                    page = c.getInt(4),
                    juz = c.getInt(5),
                    hizb = c.getInt(6),
                    rub = c.getInt(7),
                    quarterInHizb = c.getInt(8),
                    manzil = c.getInt(9),
                    ruku = c.getInt(10),
                    isSajda = c.getInt(11) == 1,
                )
                out += SearchHit(
                    ayah = ayah,
                    surahNameAr = c.getString(12),
                    snippet = ayah.textUthmani,
                    matchPositions = emptyList(),
                    rank = c.getDouble(13),
                )
            }
        }
        out
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
