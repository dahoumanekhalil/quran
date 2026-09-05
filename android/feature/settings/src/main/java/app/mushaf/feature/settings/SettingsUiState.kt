package app.mushaf.feature.settings

import app.mushaf.core.domain.model.AppSettings

/**
 * Immutable UI state for the Settings screen.
 *
 * `size` is a discretization of `settings.fontSizeSp` — the reader stores a
 * float internally for future-proofing (e.g. accessibility zoom), but the
 * user picks from three named steps chosen to preserve line breaks on the
 * 15-line Mushaf page layout (TASK-146).
 */
data class SettingsUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
) {
    val size: ReadingSize get() = ReadingSize.fromSp(settings.fontSizeSp)
}

enum class ReadingSize(val sp: Float) {
    SMALL(26f),
    MEDIUM(30f),
    LARGE(34f),
    ;

    companion object {
        fun fromSp(sp: Float): ReadingSize =
            entries.minBy { kotlin.math.abs(it.sp - sp) }
    }
}
