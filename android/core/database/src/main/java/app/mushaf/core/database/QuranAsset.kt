package app.mushaf.core.database

/**
 * Coordinates for the bundled Quran SQLite asset. Kept in one place so
 * a content version bump only edits this file (per ADR-0014).
 */
object QuranAsset {
    const val ASSET_PATH = "quran/quran.db"        // in app/src/main/assets/
    const val INSTALL_FILENAME = "quran.db"        // in context.filesDir
    const val CONTENT_VERSION = "1.1.0"
    /** Full-file SHA-256, checked on first-launch copy. Bumped alongside the DB. */
    const val CONTENT_SHA256 =
        "86e6be19da45d686146d4840b109b3ad0efb39525e366f080485589c4a2085cb"
}
