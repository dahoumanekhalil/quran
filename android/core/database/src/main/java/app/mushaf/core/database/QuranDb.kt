package app.mushaf.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.security.MessageDigest

/**
 * Read-only accessor over the bundled Quran SQLite. Per ADR-0014 the file
 * ships in assets/, is copied to internal storage on first use, verified
 * against a known SHA-256, then opened read-only.
 */
class QuranDb(context: Context) {

    val database: SQLiteDatabase

    init {
        val ctx = context.applicationContext
        val target = File(ctx.filesDir, QuranAsset.INSTALL_FILENAME)
        val needsCopy = !target.exists() || !hashMatches(target, QuranAsset.CONTENT_SHA256)
        if (needsCopy) {
            if (target.exists()) target.delete()
            ctx.assets.open(QuranAsset.ASSET_PATH).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            check(hashMatches(target, QuranAsset.CONTENT_SHA256)) {
                "Bundled Quran DB failed content-integrity check after copy. " +
                    "This should never happen — verify assets/${QuranAsset.ASSET_PATH} was not corrupted at build time."
            }
        }
        database = SQLiteDatabase.openDatabase(
            target.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY,
        )
    }

    private fun hashMatches(file: File, expectedHex: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                digest.update(buf, 0, n)
            }
        }
        val actualHex = digest.digest().joinToString("") { "%02x".format(it) }
        return actualHex.equals(expectedHex, ignoreCase = true)
    }
}
