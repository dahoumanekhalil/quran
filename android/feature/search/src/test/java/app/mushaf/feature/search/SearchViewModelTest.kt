package app.mushaf.feature.search

import app.mushaf.core.domain.model.Ayah
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import app.mushaf.core.domain.search.SearchHit
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var quranRepo: QuranRepository
    private lateinit var readingRepo: ReadingPositionRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        quranRepo = mockk(relaxed = true)
        readingRepo = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVM() = SearchViewModel(quranRepo, readingRepo)

    private fun ayah(gid: Int = 1, page: Int = 1) = Ayah(
        globalIndex = gid, surah = 1, ayah = gid, textUthmani = "بِسْمِ", page = page,
        juz = 1, hizb = 1, rub = 1, quarterInHizb = 1, manzil = 1, ruku = 1, isSajda = false,
    )

    private fun hit(gid: Int = 1, page: Int = 1) = SearchHit(
        ayah = ayah(gid = gid, page = page),
        surahNameAr = "الفاتحة",
        snippet = "بِسْمِ",
        matchPositions = emptyList(),
        rank = 0.0,
    )

    @Test
    fun initialStateIsEmpty() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        assertThat(vm.uiState.value.query).isEmpty()
        assertThat(vm.uiState.value.results).isEmpty()
        assertThat(vm.uiState.value.hasSearched).isFalse()
        assertThat(vm.uiState.value.showHint).isTrue()
    }

    @Test
    fun blankQueryDoesNotHitRepository() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onQueryChanged("   ")
        advanceTimeBy(400)
        advanceUntilIdle()

        coVerify(exactly = 0) { quranRepo.search(any(), any()) }
        assertThat(vm.uiState.value.hasSearched).isFalse()
    }

    @Test
    fun singleCharacterQueryIsBelowMinLength() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onQueryChanged("ب")
        advanceTimeBy(400)
        advanceUntilIdle()

        // Effective length after SearchNormalizer.normalize("ب") == 1 → below MIN_QUERY_LENGTH.
        coVerify(exactly = 0) { quranRepo.search(any(), any()) }
        assertThat(vm.uiState.value.hasSearched).isFalse()
    }

    @Test
    fun diacriticsOnlyQueryIsRejected() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        // Fatha + kasra + damma — all strip in the normalizer, leaving nothing.
        vm.onQueryChanged("َِ ُ")
        advanceTimeBy(400)
        advanceUntilIdle()

        coVerify(exactly = 0) { quranRepo.search(any(), any()) }
    }

    @Test
    fun twoCharacterQueryHitsRepository() = runTest(dispatcher) {
        coEvery { quranRepo.search(any(), any()) } returns listOf(hit())
        val vm = buildVM()
        advanceUntilIdle()

        vm.onQueryChanged("بم")
        advanceTimeBy(400)
        advanceUntilIdle()

        coVerify(exactly = 1) { quranRepo.search("بم", any()) }
        assertThat(vm.uiState.value.hasSearched).isTrue()
        assertThat(vm.uiState.value.results).hasSize(1)
    }

    @Test
    fun debounceCoalescesRapidTyping() = runTest(dispatcher) {
        coEvery { quranRepo.search(any(), any()) } returns emptyList()
        val vm = buildVM()
        advanceUntilIdle()

        vm.onQueryChanged("ب")
        vm.onQueryChanged("بس")
        vm.onQueryChanged("بسم")
        advanceTimeBy(400)
        advanceUntilIdle()

        // Debounce (300 ms) should coalesce; only the final value fires the search.
        coVerify(exactly = 1) { quranRepo.search("بسم", any()) }
    }

    @Test
    fun clearQueryResetsState() = runTest(dispatcher) {
        coEvery { quranRepo.search(any(), any()) } returns listOf(hit())
        val vm = buildVM()
        advanceUntilIdle()

        vm.onQueryChanged("بسم")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertThat(vm.uiState.value.results).isNotEmpty()

        vm.clearQuery()
        advanceUntilIdle()

        assertThat(vm.uiState.value.query).isEmpty()
        assertThat(vm.uiState.value.results).isEmpty()
        assertThat(vm.uiState.value.hasSearched).isFalse()
    }

    @Test
    fun openResultWritesReadingPosition() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()
        var closed = false

        vm.openResult(hit(gid = 42, page = 3)) { closed = true }
        advanceUntilIdle()

        coVerify(exactly = 1) {
            readingRepo.save(
                match { it.pageNumber == 3 && it.ayahGlobalIndex == 42 },
            )
        }
        assertThat(closed).isTrue()
    }

    @Test
    fun searchFailureSurfacesAsError() = runTest(dispatcher) {
        coEvery { quranRepo.search(any(), any()) } throws RuntimeException("DB unavailable")
        val vm = buildVM()
        advanceUntilIdle()

        vm.onQueryChanged("بسم")
        advanceTimeBy(400)
        advanceUntilIdle()

        // TASK-223: a real search failure must be visible, not silently masked
        // as "no results".
        assertThat(vm.uiState.value.showError).isTrue()
        assertThat(vm.uiState.value.error).isEqualTo("DB unavailable")
        assertThat(vm.uiState.value.showNoResults).isFalse()
    }
}
