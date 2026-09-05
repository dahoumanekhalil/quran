package app.mushaf.feature.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.BookmarksRepository
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import app.mushaf.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val readingPositionRepository: ReadingPositionRepository,
    private val settingsRepository: SettingsRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState.Initial)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    /**
     * Emissions of the page the user is viewing right now. Debounced downstream
     * before persisting to avoid a DataStore write per swipe.
     */
    private val pageChanges = MutableSharedFlow<Int>(extraBufferCapacity = 32)

    /** Last page value the ViewModel wrote to the repository — used to break the
     *  observe/save echo loop. */
    @Volatile private var lastPersistedPage: Int = -1

    // Small LRU page cache (self + one preloaded on either side).
    private val pageCache = LinkedHashMap<Int, Page>(6, 0.75f, true)
    private var preloadJob: Job? = null

    init {
        val restoredPage = savedState.get<Int>(KEY_CURRENT_PAGE)
        viewModelScope.launch {
            val initialPage = restoredPage ?: readingPositionRepository.get().pageNumber
            lastPersistedPage = initialPage
            _uiState.update { it.copy(currentPageNumber = initialPage, loading = false) }
            preload(initialPage)
            updatePageContext(initialPage)
        }

        // Observe settings.
        settingsRepository.observe()
            .onEach { s -> _uiState.update { it.copy(settings = s) } }
            .launchIn(viewModelScope)

        // Observe external reading-position changes (e.g. from NavigationScreen).
        // Drop the first (initial-load echo). Ignore anything we just wrote.
        readingPositionRepository.observe()
            .drop(1)
            .distinctUntilChanged()
            .onEach { pos ->
                if (pos.pageNumber != lastPersistedPage && pos.pageNumber != _uiState.value.currentPageNumber) {
                    lastPersistedPage = pos.pageNumber
                    _uiState.update { it.copy(currentPageNumber = pos.pageNumber) }
                    savedState[KEY_CURRENT_PAGE] = pos.pageNumber
                    preload(pos.pageNumber)
                }
            }
            .launchIn(viewModelScope)

        // Debounced persistence of position changes originating from the pager.
        pageChanges
            .filter { it != lastPersistedPage }
            .debounce(500)
            .onEach { page ->
                lastPersistedPage = page
                readingPositionRepository.save(
                    ReadingPosition(pageNumber = page, updatedAtEpochMillis = System.currentTimeMillis()),
                )
            }
            .launchIn(viewModelScope)

        // Bookmark state for the *current* page — switches source whenever the
        // page changes so the ribbon icon is always in sync.
        _uiState
            .map { it.currentPageNumber }
            .distinctUntilChanged()
            .flatMapLatest { page -> bookmarksRepository.observeIsBookmarked(page) }
            .onEach { flag -> _uiState.update { it.copy(isCurrentPageBookmarked = flag) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ReaderEvent) {
        when (event) {
            is ReaderEvent.PageChanged -> onPageChanged(event.newPageNumber)
            is ReaderEvent.JumpToPage -> onPageChanged(event.pageNumber)
            ReaderEvent.ToggleControls -> _uiState.update { it.copy(isControlsVisible = !it.isControlsVisible) }
            ReaderEvent.HideControls -> _uiState.update { it.copy(isControlsVisible = false) }
            ReaderEvent.ToggleBookmark -> toggleBookmark()
        }
    }

    private fun toggleBookmark() {
        val page = _uiState.value.currentPageNumber
        val isBookmarked = _uiState.value.isCurrentPageBookmarked
        viewModelScope.launch {
            if (isBookmarked) bookmarksRepository.remove(page) else bookmarksRepository.add(page)
        }
    }

    suspend fun loadPage(pageNumber: Int): Page {
        pageCache[pageNumber]?.let { return it }
        val page = quranRepository.getPage(pageNumber)
        pageCache[pageNumber] = page
        while (pageCache.size > MAX_CACHED_PAGES) {
            val eldest = pageCache.entries.iterator().next()
            pageCache.remove(eldest.key)
        }
        return page
    }

    private fun onPageChanged(newPage: Int) {
        if (newPage !in 1..604) return
        if (newPage == _uiState.value.currentPageNumber) return
        _uiState.update { it.copy(currentPageNumber = newPage) }
        savedState[KEY_CURRENT_PAGE] = newPage
        pageChanges.tryEmit(newPage)
        preload(newPage)
        viewModelScope.launch { updatePageContext(newPage) }
    }

    /**
     * Resolves the surah name (Arabic) and juz number for the given page so
     * the top chrome can show reading context. Uses the repository's in-memory
     * juz cache — no extra DAO round-trip after the first call.
     */
    private suspend fun updatePageContext(pageNumber: Int) {
        val page = loadPage(pageNumber)
        val surahName = page.ayahs.firstOrNull()?.surahNameAr
        val allJuz = runCatching { quranRepository.getAllJuz() }.getOrDefault(emptyList())
        val gid = page.firstAyahGlobalIndex
        val juzNumber = allJuz.firstOrNull { gid in it.firstAyahGlobalIndex..it.lastAyahGlobalIndex }?.number
        _uiState.update { it.copy(currentSurahNameAr = surahName, currentJuzNumber = juzNumber) }
    }

    private fun preload(page: Int) {
        preloadJob?.cancel()
        preloadJob = viewModelScope.launch {
            listOf(page - 1, page + 1, page).filter { it in 1..604 }.forEach { p ->
                if (p !in pageCache) loadPage(p)
            }
        }
    }

    private companion object {
        const val KEY_CURRENT_PAGE = "reader_current_page"
        const val MAX_CACHED_PAGES = 5
    }
}
