package app.mushaf.feature.search

import app.mushaf.core.domain.search.SearchHit

/**
 * Immutable UI state for the Search screen (per ADR-0006 / ADR-0012).
 *
 *  - `query` mirrors the text-field content.
 *  - `results` is the last completed search's hits.
 *  - `loading` shows a subtle progress indicator during a query.
 *  - `hasSearched` distinguishes "user hasn't typed yet" (show hint) from
 *    "searched but no matches" (show quiet empty-state).
 */
data class SearchUiState(
    val query: String = "",
    val results: List<SearchHit> = emptyList(),
    val loading: Boolean = false,
    val hasSearched: Boolean = false,
) {
    val showHint: Boolean get() = !hasSearched && query.isBlank()
    val showNoResults: Boolean get() = hasSearched && !loading && results.isEmpty() && query.isNotBlank()
}
