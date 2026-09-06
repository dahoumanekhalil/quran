package app.mushaf.core.data

import app.cash.turbine.test
import app.mushaf.core.datastore.UserPreferencesStore
import app.mushaf.core.domain.model.Bookmark
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksRepositoryImplTest {

    private val bookmarksFlow = MutableStateFlow<List<Bookmark>>(emptyList())

    private fun mkStore(initial: List<Bookmark> = emptyList()): UserPreferencesStore {
        bookmarksFlow.value = initial
        return mockk(relaxed = true) {
            every { bookmarks } returns bookmarksFlow
            coEvery { bookmarksOnce() } answers { bookmarksFlow.value }
        }
    }

    @Test
    fun observeYieldsUnderlyingFlow() = runTest {
        val store = mkStore(listOf(bookmark(42)))
        val repo = BookmarksRepositoryImpl(store)

        repo.observe().test {
            val first = awaitItem()
            assertThat(first).hasSize(1)
            assertThat(first.first().pageNumber).isEqualTo(42)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeIsBookmarkedTracksPage() = runTest {
        val store = mkStore(listOf(bookmark(42)))
        val repo = BookmarksRepositoryImpl(store)

        repo.observeIsBookmarked(42).test {
            assertThat(awaitItem()).isTrue()
            cancelAndConsumeRemainingEvents()
        }
        repo.observeIsBookmarked(100).test {
            assertThat(awaitItem()).isFalse()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeIsBookmarkedDoesNotRepeat() = runTest {
        val store = mkStore(listOf(bookmark(42)))
        val repo = BookmarksRepositoryImpl(store)

        repo.observeIsBookmarked(42).test {
            assertThat(awaitItem()).isTrue()
            // Emit an unrelated bookmark set change — same isBookmarked result → distinctUntilChanged drops.
            bookmarksFlow.value = listOf(bookmark(42), bookmark(50))
            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun isBookmarkedReadsSnapshot() = runTest {
        val store = mkStore(listOf(bookmark(42), bookmark(50)))
        val repo = BookmarksRepositoryImpl(store)

        assertThat(repo.isBookmarked(42)).isTrue()
        assertThat(repo.isBookmarked(51)).isFalse()
    }

    @Test
    fun addRejectsOutOfRange() {
        val store = mkStore()
        val repo = BookmarksRepositoryImpl(store)

        assertThrows<IllegalArgumentException> { runBlocking { repo.add(0) } }
        assertThrows<IllegalArgumentException> { runBlocking { repo.add(605) } }
    }

    @Test
    fun addAppliesTransformation() = runTest {
        val store = mkStore()
        val transformSlot = slot<(List<Bookmark>) -> List<Bookmark>>()
        coEvery { store.updateBookmarks(capture(transformSlot)) } answers { }
        val repo = BookmarksRepositoryImpl(store)

        repo.add(42)

        val result = transformSlot.captured(emptyList())
        assertThat(result).hasSize(1)
        assertThat(result.first().pageNumber).isEqualTo(42)
        assertThat(result.first().ayahGlobalIndex).isNull()
    }

    @Test
    fun addIsIdempotentForSamePage() = runTest {
        val existing = listOf(bookmark(42))
        val store = mkStore(existing)
        val transformSlot = slot<(List<Bookmark>) -> List<Bookmark>>()
        coEvery { store.updateBookmarks(capture(transformSlot)) } answers { }
        val repo = BookmarksRepositoryImpl(store)

        repo.add(42)

        // Applying the transform to the existing list should be a no-op.
        val result = transformSlot.captured(existing)
        assertThat(result).isEqualTo(existing)
    }

    @Test
    fun removeFiltersMatchingPage() = runTest {
        val existing = listOf(bookmark(42), bookmark(50), bookmark(100))
        val store = mkStore(existing)
        val transformSlot = slot<(List<Bookmark>) -> List<Bookmark>>()
        coEvery { store.updateBookmarks(capture(transformSlot)) } answers { }
        val repo = BookmarksRepositoryImpl(store)

        repo.remove(50)

        val result = transformSlot.captured(existing)
        assertThat(result.map { it.pageNumber }).containsExactly(42, 100).inOrder()
    }

    @Test
    fun removeAbsentPageIsNoOp() = runTest {
        val existing = listOf(bookmark(42))
        val store = mkStore(existing)
        val transformSlot = slot<(List<Bookmark>) -> List<Bookmark>>()
        coEvery { store.updateBookmarks(capture(transformSlot)) } answers { }
        val repo = BookmarksRepositoryImpl(store)

        repo.remove(999)

        val result = transformSlot.captured(existing)
        assertThat(result).isEqualTo(existing)
    }

    private fun bookmark(page: Int) = Bookmark(
        pageNumber = page,
        ayahGlobalIndex = null,
        createdAtEpochMillis = 1_000L * page,
    )
}
