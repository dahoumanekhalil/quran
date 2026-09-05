package app.mushaf.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mushaf.core.domain.model.MushafFont
import app.mushaf.core.domain.model.ThemeMode
import app.mushaf.core.domain.repository.SettingsRepository
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
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.observe()
            .onEach { s -> _uiState.update { it.copy(loading = false, settings = s) } }
            .launchIn(viewModelScope)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.update { it.copy(themeMode = mode) } }
    }

    fun setSize(size: ReadingSize) {
        viewModelScope.launch { settingsRepository.update { it.copy(fontSizeSp = size.sp) } }
    }

    fun setFont(font: MushafFont) {
        viewModelScope.launch { settingsRepository.update { it.copy(font = font) } }
    }

    fun setKeepScreenOn(on: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(keepScreenOn = on) } }
    }
}
