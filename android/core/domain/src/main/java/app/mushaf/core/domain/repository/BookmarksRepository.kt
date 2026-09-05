package app.mushaf.core.domain.repository

import app.mushaf.core.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

/**
 * Page-scoped bookmark storage per ADR-0025.
 *
 * Adding an already-bookmarked page is a no-op — the ribbon is either up or
 * down. `observeIsBookmarked` is intended for the reader chrome toggle,
 * which flips as the user swipes between pages.
 */
interface BookmarksRepository {
    fun observe(): Flow<List<Bookmark>>
    fun observeIsBookmarked(pageNumber: Int): Flow<Boolean>
    suspend fun isBookmarked(pageNumber: Int): Boolean
    suspend fun add(pageNumber: Int)
    suspend fun remove(pageNumber: Int)
}
