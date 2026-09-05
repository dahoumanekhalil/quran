package app.mushaf.feature.navigation

import app.mushaf.core.domain.model.Bookmark
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Surah

data class NavigationUiState(
    val loading: Boolean = true,
    val surahs: List<Surah> = emptyList(),
    val juz: List<Juz> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val selectedTab: NavTab = NavTab.SURAH,
    val jumpPageInput: String = "",
    val jumpPageError: String? = null,
)

enum class NavTab { SURAH, JUZ, PAGE, BOOKMARKS }
