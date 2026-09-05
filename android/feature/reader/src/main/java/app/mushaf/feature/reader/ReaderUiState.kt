package app.mushaf.feature.reader

import app.mushaf.core.domain.model.AppSettings

/**
 * Immutable UI state for the Reader (per ADR-0006 / ADR-0012).
 *
 * `currentPageNumber` and `settings` are the source of truth; the current
 * page's content is hydrated lazily by the ViewModel through [ReaderViewModel.loadPage].
 *
 * `isControlsVisible` drives the tap-to-toggle chrome + immersive system-bar
 * behavior (TASK-074, TASK-075). Default true so first-launch users see the
 * navigation menu; the reader remains one tap away from a distraction-free view.
 */
data class ReaderUiState(
    val currentPageNumber: Int = 1,
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = true,
    val isControlsVisible: Boolean = true,
) {
    companion object {
        val Initial = ReaderUiState()
    }
}

sealed interface ReaderEvent {
    data class PageChanged(val newPageNumber: Int) : ReaderEvent
    data class JumpToPage(val pageNumber: Int) : ReaderEvent
    data object ToggleControls : ReaderEvent
    data object HideControls : ReaderEvent
}
