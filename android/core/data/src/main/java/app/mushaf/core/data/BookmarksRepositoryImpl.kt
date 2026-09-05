package app.mushaf.core.data

import app.mushaf.core.datastore.UserPreferencesStore
import app.mushaf.core.domain.model.Bookmark
import app.mushaf.core.domain.repository.BookmarksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksRepositoryImpl @Inject constructor(
    private val store: UserPreferencesStore,
) : BookmarksRepository {

    override fun observe(): Flow<List<Bookmark>> = store.bookmarks

    override fun observeIsBookmarked(pageNumber: Int): Flow<Boolean> =
        store.bookmarks
            .map { list -> list.any { it.pageNumber == pageNumber } }
            .distinctUntilChanged()

    override suspend fun isBookmarked(pageNumber: Int): Boolean =
        store.bookmarksOnce().any { it.pageNumber == pageNumber }

    override suspend fun add(pageNumber: Int) {
        require(pageNumber in 1..604) { "Page $pageNumber out of range 1..604" }
        store.updateBookmarks { current ->
            // Idempotent: adding an already-bookmarked page returns the current list unchanged.
            if (current.any { it.pageNumber == pageNumber && it.ayahGlobalIndex == null }) return@updateBookmarks current
            current + Bookmark(
                pageNumber = pageNumber,
                ayahGlobalIndex = null,
                createdAtEpochMillis = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun remove(pageNumber: Int) {
        store.updateBookmarks { current -> current.filterNot { it.pageNumber == pageNumber } }
    }
}
