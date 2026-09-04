package app.mushaf.prototype

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Minimal Quran DB accessor for the rendering prototype.
 *
 * Copies the bundled asset (assets/quran/quran.db) to internal storage on
 * first use, then opens it read-only. Prototype-grade: no integrity hash
 * check, no version awareness, no coroutine wrappers — the production
 * version lives in Phase 4 (:core:database, per ADR-0014).
 */
class Quran(context: Context) {

    private val db: SQLiteDatabase

    init {
        val target = File(context.filesDir, DB_FILENAME)
        if (!target.exists()) {
            context.assets.open("quran/$DB_FILENAME").use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        db = SQLiteDatabase.openDatabase(
            target.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY,
        )
    }

    fun getPage(pageNumber: Int): PageData {
        val ayahs = mutableListOf<AyahOnPage>()
        db.rawQuery(
            """
            SELECT a.global_index, a.surah, a.ayah, a.text_uthmani, s.name_ar, s.bismillah_pre
            FROM ayahs a JOIN surahs s ON s.number = a.surah
            WHERE a.page = ?
            ORDER BY a.global_index
            """.trimIndent(),
            arrayOf(pageNumber.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                ayahs += AyahOnPage(
                    globalIndex = c.getInt(0),
                    surah = c.getInt(1),
                    ayah = c.getInt(2),
                    text = c.getString(3),
                    surahNameAr = c.getString(4),
                    bismillahPre = c.getInt(5) == 1,
                )
            }
        }
        return PageData(pageNumber = pageNumber, ayahs = ayahs)
    }

    companion object {
        const val DB_FILENAME = "quran.db"
        const val TOTAL_PAGES = 604
    }
}

data class PageData(
    val pageNumber: Int,
    val ayahs: List<AyahOnPage>,
)

data class AyahOnPage(
    val globalIndex: Int,
    val surah: Int,
    val ayah: Int,
    val text: String,
    val surahNameAr: String,
    val bismillahPre: Boolean,
)
