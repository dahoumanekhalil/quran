package app.mushaf.core.database

/**
 * Coordinates for the bundled Quran SQLite asset. Kept in one place so
 * a content version bump only edits this file (per ADR-0014).
 */
object QuranAsset {
    const val ASSET_PATH = "quran/quran.db"        // in app/src/main/assets/
    const val INSTALL_FILENAME = "quran.db"        // in context.filesDir
    const val CONTENT_VERSION = "1.0.0"
    /** Full-file SHA-256, checked on first-launch copy. Bumped alongside the DB. */
    const val CONTENT_SHA256 =
        "0ba78b6ab99f57a5688adc572f35ccf0568716a48d4424378d73cfd91c0b093e"
}
