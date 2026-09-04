package app.mushaf.feature.reader

import app.mushaf.core.domain.model.AppSettings
import app.mushaf.core.domain.model.Page

/**
 * Immutable UI state for the Reader (per ADR-0006 / ADR-0012).
 *
 * `currentPageNumber` and `settings` are the source of truth; `currentPage`
 * is cached on demand and hydrated lazily by the ViewModel.
 */
data class ReaderUiState(
    val currentPageNumber: Int = 1,
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = true,
) {
    companion object {
        val Initial = ReaderUiState()
    }
}

sealed interface ReaderEvent {
    data class PageChanged(val newPageNumber: Int) : ReaderEvent
    data class JumpToPage(val pageNumber: Int) : ReaderEvent
}
