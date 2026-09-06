package app.mushaf.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest

/**
 * Read-only accessor over the bundled Quran SQLite. Per ADR-0014 the file
 * ships in assets/, is copied to internal storage on first use, verified
 * against a known SHA-256, then opened read-only.
 *
 * The copy + hash + open is I/O-heavy (3.2 MB + a SHA-256 pass) so it is
 * deliberately deferred until the first [database] call and gated behind a
 * suspend function. All callers must access the DB from a coroutine on
 * `Dispatchers.IO`. The DAO does this via `@IoDispatcher` — first access
 * therefore runs off the main thread even on cold start.
 */
class QuranDb(private val context: Context) {

    private val mutex = Mutex()

    @Volatile
    private var _database: SQLiteDatabase? = null

    /** Suspend accessor. First call performs copy + integrity check on the calling dispatcher (expected: IO). */
    suspend fun database(): SQLiteDatabase {
        _database?.let { return it }
        return mutex.withLock {
            _database ?: openDatabase().also { _database = it }
        }
    }

    private fun openDatabase(): SQLiteDatabase {
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
        return SQLiteDatabase.openDatabase(
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
