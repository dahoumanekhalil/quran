package app.mushaf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mushaf.core.domain.model.ThemeMode
import app.mushaf.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Top-level ViewModel scoped to the Activity — exposes the theme choice so
 * [MainActivity] can drive [app.mushaf.core.designsystem.MushafTheme] without
 * threading a repository through Compose parameters.
 */
@HiltViewModel
class MushafRootViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.observe()
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
}
