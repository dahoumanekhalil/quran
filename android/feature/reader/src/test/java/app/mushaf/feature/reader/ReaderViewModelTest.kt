package app.mushaf.feature.reader

import androidx.lifecycle.SavedStateHandle
import app.mushaf.core.domain.model.AppSettings
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.BookmarksRepository
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import app.mushaf.core.domain.repository.SettingsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ReaderViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var quranRepo: QuranRepository
    private lateinit var readingRepo: ReadingPositionRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var bookmarksRepo: BookmarksRepository
    private val positionFlow = MutableStateFlow(ReadingPosition(pageNumber = 42))
    private val settingsFlow = MutableStateFlow(AppSettings())
    private val isBookmarkedFlow = MutableStateFlow(false)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        quranRepo = mockk(relaxed = true) {
            coEvery { getPage(any()) } returns Page(
                number = 1,
                firstAyahGlobalIndex = 1,
                lastAyahGlobalIndex = 7,
                surahsOnPage = listOf(1),
                juzStartsOnPage = listOf(1),
                ayahs = emptyList(),
            )
        }
        readingRepo = mockk(relaxed = true) {
            every { observe() } returns positionFlow
            coEvery { get() } returns positionFlow.value
        }
        settingsRepo = mockk(relaxed = true) {
            every { observe() } returns settingsFlow
        }
        bookmarksRepo = mockk(relaxed = true) {
            every { observeIsBookmarked(any()) } returns isBookmarkedFlow
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVM(savedPage: Int? = null): ReaderViewModel {
        val saved = if (savedPage != null) {
            SavedStateHandle(mapOf("reader_current_page" to savedPage))
        } else {
            SavedStateHandle()
        }
        return ReaderViewModel(quranRepo, readingRepo, settingsRepo, bookmarksRepo, saved)
    }

    @Test
    fun restoresFromRepositoryWhenSavedStateEmpty() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        assertThat(vm.uiState.value.currentPageNumber).isEqualTo(42)
        assertThat(vm.uiState.value.loading).isFalse()
    }

    @Test
    fun restoresFromSavedStateWhenPresent() = runTest(dispatcher) {
        val vm = buildVM(savedPage = 100)
        advanceUntilIdle()

        assertThat(vm.uiState.value.currentPageNumber).isEqualTo(100)
    }

    @Test
    fun pageChangedUpdatesCurrentPage() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onEvent(ReaderEvent.PageChanged(50))
        advanceUntilIdle()

        assertThat(vm.uiState.value.currentPageNumber).isEqualTo(50)
    }

    @Test
    fun pageChangedRejectsOutOfRange() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onEvent(ReaderEvent.PageChanged(0))
        vm.onEvent(ReaderEvent.PageChanged(605))
        advanceUntilIdle()

        assertThat(vm.uiState.value.currentPageNumber).isEqualTo(42)
    }

    @Test
    fun toggleControlsFlipsVisibility() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        assertThat(vm.uiState.value.isControlsVisible).isTrue()
        vm.onEvent(ReaderEvent.ToggleControls)
        assertThat(vm.uiState.value.isControlsVisible).isFalse()
        vm.onEvent(ReaderEvent.ToggleControls)
        assertThat(vm.uiState.value.isControlsVisible).isTrue()
    }

    @Test
    fun hideControlsSetsFalse() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onEvent(ReaderEvent.HideControls)

        assertThat(vm.uiState.value.isControlsVisible).isFalse()
    }

    @Test
    fun debouncedSaveDoesNotFireBeforeWindow() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onEvent(ReaderEvent.PageChanged(50))
        advanceTimeBy(400)

        coVerify(exactly = 0) { readingRepo.save(match { it.pageNumber == 50 }) }
    }

    @Test
    fun debouncedSaveFiresAfterWindow() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        vm.onEvent(ReaderEvent.PageChanged(50))
        advanceTimeBy(600)
        advanceUntilIdle()

        coVerify(atLeast = 1) { readingRepo.save(match { it.pageNumber == 50 }) }
    }

    @Test
    fun rapidPageChangesCoalesceToOneSave() = runTest(dispatcher) {
        val vm = buildVM()
        advanceUntilIdle()

        (43..60).forEach { vm.onEvent(ReaderEvent.PageChanged(it)) }
        advanceTimeBy(600)
        advanceUntilIdle()

        // Only the final page (60) should be persisted after the debounce window.
        coVerify(exactly = 1) { readingRepo.save(match { it.pageNumber == 60 }) }
    }
}
