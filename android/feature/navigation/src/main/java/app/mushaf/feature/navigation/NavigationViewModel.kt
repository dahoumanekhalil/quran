package app.mushaf.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.BookmarksRepository
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val readingPositionRepository: ReadingPositionRepository,
    private val bookmarksRepository: BookmarksRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val surahs = quranRepository.getAllSurahs()
            val juz = quranRepository.getAllJuz()
            _uiState.update { it.copy(loading = false, surahs = surahs, juz = juz) }
        }
        // Reactive bookmarks — list updates whenever the reader ribbon toggles.
        bookmarksRepository.observe()
            .onEach { list -> _uiState.update { it.copy(bookmarks = list) } }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: NavTab) {
        _uiState.update { it.copy(selectedTab = tab, jumpPageError = null) }
    }

    fun onJumpPageInputChanged(text: String) {
        _uiState.update { it.copy(jumpPageInput = text.filter { c -> c.isDigit() }.take(3), jumpPageError = null) }
    }

    /**
     * Fire-and-forget: write the requested reading position, then invoke [andThen]
     * on the main dispatcher (typically the caller pops the back stack).
     */
    fun jumpToPage(pageNumber: Int, andThen: () -> Unit) {
        if (pageNumber !in 1..604) return
        viewModelScope.launch {
            readingPositionRepository.save(
                ReadingPosition(
                    pageNumber = pageNumber,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            andThen()
        }
    }

    fun jumpToSurah(surahNumber: Int, andThen: () -> Unit) {
        viewModelScope.launch {
            val page = quranRepository.pageOfSurahStart(surahNumber)
            readingPositionRepository.save(
                ReadingPosition(pageNumber = page, updatedAtEpochMillis = System.currentTimeMillis()),
            )
            andThen()
        }
    }

    fun jumpToJuz(juzNumber: Int, andThen: () -> Unit) {
        viewModelScope.launch {
            val page = quranRepository.pageOfJuzStart(juzNumber)
            readingPositionRepository.save(
                ReadingPosition(pageNumber = page, updatedAtEpochMillis = System.currentTimeMillis()),
            )
            andThen()
        }
    }

    fun removeBookmark(pageNumber: Int) {
        viewModelScope.launch { bookmarksRepository.remove(pageNumber) }
    }

    /** Returns the target page after validating, or null; sets error state as a side effect. */
    fun resolveJumpPage(): Int? {
        val n = _uiState.value.jumpPageInput.toIntOrNull()
        return if (n == null || n !in 1..604) {
            _uiState.update { it.copy(jumpPageError = "Enter a page 1..604") }
            null
        } else n
    }
}
