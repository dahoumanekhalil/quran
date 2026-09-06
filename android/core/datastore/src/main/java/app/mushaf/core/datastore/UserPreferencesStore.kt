package app.mushaf.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.mushaf.core.domain.model.AppSettings
import app.mushaf.core.domain.model.Bookmark
import app.mushaf.core.domain.model.MushafFont
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesStore @Inject constructor(
    private val context: Context,
) {
    private val ds get() = context.userPrefsDataStore

    // --- Reading position ---
    val readingPosition: Flow<ReadingPosition> = ds.data.map { prefs ->
        // Clamp on read: if the persisted page is out-of-range (POST-MVP schema
        // drift, tampered file, downgraded install), fall back to page 1 rather
        // than crashing the pager on an invalid page index.
        val rawPage = prefs[KEY_LAST_PAGE] ?: 1
        val safePage = if (rawPage in 1..604) rawPage else 1
        ReadingPosition(
            pageNumber = safePage,
            ayahGlobalIndex = prefs[KEY_LAST_AYAH].takeIf { it != null && it > 0 },
            updatedAtEpochMillis = prefs[KEY_LAST_UPDATED] ?: 0L,
        )
    }

    suspend fun readingPositionOnce(): ReadingPosition = readingPosition.first()

    suspend fun saveReadingPosition(position: ReadingPosition) {
        ds.edit { prefs ->
            prefs[KEY_LAST_PAGE] = position.pageNumber
            val ayah = position.ayahGlobalIndex
            if (ayah != null) {
                prefs[KEY_LAST_AYAH] = ayah
            } else {
                prefs.remove(KEY_LAST_AYAH)
            }
            prefs[KEY_LAST_UPDATED] = position.updatedAtEpochMillis
        }
    }

    // --- Settings ---
    val settings: Flow<AppSettings> = ds.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            font = prefs[KEY_FONT]?.let { runCatching { MushafFont.valueOf(it) }.getOrNull() }
                ?: MushafFont.KFGQPC,
            fontSizeSp = prefs[KEY_FONT_SIZE] ?: 30f,
            lineHeightMultiplier = prefs[KEY_LINE_HEIGHT] ?: 2.2f,
            keepScreenOn = prefs[KEY_KEEP_SCREEN_ON] ?: true,
        )
    }

    suspend fun settingsOnce(): AppSettings = settings.first()

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = settingsOnce()
        val next = transform(current)
        ds.edit { prefs ->
            prefs[KEY_THEME] = next.themeMode.name
            prefs[KEY_FONT] = next.font.name
            prefs[KEY_FONT_SIZE] = next.fontSizeSp
            prefs[KEY_LINE_HEIGHT] = next.lineHeightMultiplier
            prefs[KEY_KEEP_SCREEN_ON] = next.keepScreenOn
        }
    }

    // --- Bookmarks (ADR-0025: DataStore-JSON deviation from ADR-0007) ---
    val bookmarks: Flow<List<Bookmark>> = ds.data.map { prefs ->
        parseBookmarks(prefs[KEY_BOOKMARKS_JSON])
    }

    suspend fun bookmarksOnce(): List<Bookmark> = bookmarks.first()

    /**
     * Read-modify-write on the bookmarks list. Duplicates on `(pageNumber,
     * ayahGlobalIndex)` are silently dropped so the reader ribbon stays a
     * single toggle. Sort order: `createdAtEpochMillis` descending.
     */
    suspend fun updateBookmarks(transform: (List<Bookmark>) -> List<Bookmark>) {
        val current = bookmarksOnce()
        val next = transform(current)
            .distinctBy { it.pageNumber to it.ayahGlobalIndex }
            .sortedByDescending { it.createdAtEpochMillis }
        ds.edit { prefs ->
            prefs[KEY_BOOKMARKS_JSON] = JSON.encodeToString(BOOKMARK_LIST_SERIALIZER, next.map(BookmarkDto::from))
        }
    }

    private fun parseBookmarks(raw: String?): List<Bookmark> {
        if (raw.isNullOrEmpty()) return emptyList()
        return runCatching {
            JSON.decodeFromString(BOOKMARK_LIST_SERIALIZER, raw)
                .map(BookmarkDto::toDomain)
                // Defense-in-depth: reject bookmarks referencing an out-of-range page
                // (only possible via manual file edit or downgrade of a POST-MVP schema).
                .filter { it.pageNumber in 1..604 }
        }.getOrDefault(emptyList())
    }

    @Serializable
    private data class BookmarkDto(
        val page: Int,
        val ayahGid: Int? = null,
        val createdAt: Long,
    ) {
        fun toDomain() = Bookmark(pageNumber = page, ayahGlobalIndex = ayahGid, createdAtEpochMillis = createdAt)

        companion object {
            fun from(b: Bookmark) = BookmarkDto(page = b.pageNumber, ayahGid = b.ayahGlobalIndex, createdAt = b.createdAtEpochMillis)
        }
    }

    private companion object {
        val KEY_LAST_PAGE: Preferences.Key<Int> = intPreferencesKey("last_page")
        val KEY_LAST_AYAH: Preferences.Key<Int> = intPreferencesKey("last_ayah_global_index")
        val KEY_LAST_UPDATED: Preferences.Key<Long> = longPreferencesKey("last_updated_millis")

        val KEY_THEME: Preferences.Key<String> = stringPreferencesKey("theme_mode")
        val KEY_FONT: Preferences.Key<String> = stringPreferencesKey("mushaf_font")
        val KEY_FONT_SIZE: Preferences.Key<Float> = floatPreferencesKey("font_size_sp")
        val KEY_LINE_HEIGHT: Preferences.Key<Float> = floatPreferencesKey("line_height")
        val KEY_KEEP_SCREEN_ON: Preferences.Key<Boolean> = booleanPreferencesKey("keep_screen_on")

        val KEY_BOOKMARKS_JSON: Preferences.Key<String> = stringPreferencesKey("bookmarks_json")

        val JSON = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val BOOKMARK_LIST_SERIALIZER = ListSerializer(BookmarkDto.serializer())
    }
}
