package app.mushaf.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mushaf.core.designsystem.MushafColors
import app.mushaf.core.domain.model.MushafFont
import app.mushaf.core.domain.model.ThemeMode

@Composable
fun SettingsRoute(
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        state = state,
        onSetTheme = viewModel::setThemeMode,
        onSetSize = viewModel::setSize,
        onSetFont = viewModel::setFont,
        onSetKeepScreenOn = viewModel::setKeepScreenOn,
        onOpenAbout = onOpenAbout,
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    onSetTheme: (ThemeMode) -> Unit,
    onSetSize: (ReadingSize) -> Unit,
    onSetFont: (MushafFont) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onOpenAbout: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            textAlign = TextAlign.Center,
        )

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            item { SectionHeader("Appearance") }
            item {
                RadioGroup(
                    label = "Theme",
                    options = ThemeMode.entries.toList(),
                    selected = state.settings.themeMode,
                    labelOf = ::themeLabel,
                    onSelected = onSetTheme,
                )
            }

            item { SectionHeader("Reading") }
            item {
                RadioGroup(
                    label = "Text size",
                    options = ReadingSize.entries.toList(),
                    selected = state.size,
                    labelOf = ::sizeLabel,
                    onSelected = onSetSize,
                )
            }
            item {
                RadioGroup(
                    label = "Mushaf font",
                    options = MushafFont.entries.toList(),
                    selected = state.settings.font,
                    labelOf = ::fontLabel,
                    onSelected = onSetFont,
                )
            }
            item {
                SwitchRow(
                    label = "Keep screen on while reading",
                    checked = state.settings.keepScreenOn,
                    onCheckedChange = onSetKeepScreenOn,
                )
            }

            item { SectionHeader("About") }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAbout() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("About Mushaf", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MushafColors.Muted)
                }
                HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MushafColors.Muted,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun <T> RadioGroup(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Column(Modifier.padding(top = 4.dp)) {
            options.forEach { opt ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(opt) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RadioButton(selected = opt == selected, onClick = { onSelected(opt) })
                    Text(labelOf(opt), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f), modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
}

private fun themeLabel(mode: ThemeMode) = when (mode) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "Follow system"
}

private fun sizeLabel(size: ReadingSize) = when (size) {
    ReadingSize.SMALL -> "Small"
    ReadingSize.MEDIUM -> "Medium"
    ReadingSize.LARGE -> "Large"
}

private fun fontLabel(font: MushafFont) = when (font) {
    MushafFont.KFGQPC -> "KFGQPC Uthmanic Hafs"
    MushafFont.AMIRI_QURAN -> "Amiri Quran"
}
