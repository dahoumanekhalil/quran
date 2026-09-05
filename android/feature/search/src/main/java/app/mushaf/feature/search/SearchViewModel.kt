package app.mushaf.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val readingPositionRepository: ReadingPositionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Debounced query pipeline: user input → 300 ms of quiet → search.
        // The 300 ms was picked to feel snappy without hammering FTS on every
        // keystroke (typical Arabic typing bursts are ~150-250 ms/char).
        _uiState
            .map { it.query }
            .distinctUntilChanged()
            .debounce(300)
            .onEach { runSearch(it) }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun clearQuery() {
        _uiState.update { SearchUiState() }
    }

    /**
     * Fire-and-forget navigation intent: writes the target page to the reading
     * position store. Reader observes and syncs. `andThen` is typically
     * `nav.popBackStack()`.
     */
    fun openResult(hit: app.mushaf.core.domain.search.SearchHit, andThen: () -> Unit) {
        viewModelScope.launch {
            readingPositionRepository.save(
                ReadingPosition(
                    pageNumber = hit.ayah.page,
                    ayahGlobalIndex = hit.ayah.globalIndex,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            andThen()
        }
    }

    private suspend fun runSearch(rawQuery: String) {
        if (rawQuery.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), loading = false, hasSearched = false) }
            return
        }
        _uiState.update { it.copy(loading = true) }
        val hits = runCatching { quranRepository.search(rawQuery) }.getOrDefault(emptyList())
        _uiState.update {
            // Only apply if the query hasn't changed since we launched.
            if (it.query == rawQuery) {
                it.copy(results = hits, loading = false, hasSearched = true)
            } else {
                it
            }
        }
    }
}
