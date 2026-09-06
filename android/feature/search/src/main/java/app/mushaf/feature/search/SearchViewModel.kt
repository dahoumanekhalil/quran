package app.mushaf.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import app.mushaf.core.domain.search.SearchNormalizer
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
        // Roadmap TASK-206: require ≥ 2 characters *after normalization*.
        // A single letter (or a query that's entirely diacritics/tatweel that
        // normalize away to nothing) is too broad to be useful and would run
        // FTS across most of the corpus for no user benefit.
        val effective = SearchNormalizer.normalize(rawQuery)
        if (effective.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(results = emptyList(), loading = false, hasSearched = false, error = null) }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        val outcome = runCatching { quranRepository.search(rawQuery) }
        _uiState.update {
            // Only apply if the query hasn't changed since we launched.
            if (it.query != rawQuery) return@update it
            outcome.fold(
                onSuccess = { hits ->
                    it.copy(results = hits, loading = false, hasSearched = true, error = null)
                },
                onFailure = { e ->
                    // Distinguish "search failed" from "no matches" (TASK-223).
                    it.copy(
                        results = emptyList(),
                        loading = false,
                        hasSearched = true,
                        error = e.message ?: e::class.simpleName ?: "Search failed",
                    )
                },
            )
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
