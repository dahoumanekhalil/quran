package app.mushaf.feature.navigation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mushaf.core.designsystem.MushafArabicFont
import app.mushaf.core.designsystem.MushafColors
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Surah

@Composable
fun NavigationRoute(
    onJumpToPage: (Int) -> Unit,
    onClose: () -> Unit,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavigationContent(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onSurahClick = { surah -> viewModel.jumpToSurah(surah.number) { onClose() } },
        onJuzClick   = { juz   -> viewModel.jumpToJuz(juz.number)   { onClose() } },
        onJumpInputChanged = viewModel::onJumpPageInputChanged,
        onJumpConfirm = {
            val page = viewModel.resolveJumpPage()
            if (page != null) {
                viewModel.jumpToPage(page) {
                    onJumpToPage(page)
                    onClose()
                }
            }
        },
    )
}

@Composable
fun NavigationContent(
    state: NavigationUiState,
    onTabSelected: (NavTab) -> Unit,
    onSurahClick: (Surah) -> Unit,
    onJuzClick: (Juz) -> Unit,
    onJumpInputChanged: (String) -> Unit,
    onJumpConfirm: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Text(
            "Navigation",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            textAlign = TextAlign.Center,
        )
        TabRow(selectedTabIndex = state.selectedTab.ordinal) {
            NavTab.entries.forEach { t ->
                Tab(
                    selected = state.selectedTab == t,
                    onClick = { onTabSelected(t) },
                    text = { Text(t.label()) },
                )
            }
        }
        Box(Modifier.fillMaxSize()) {
            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (state.selectedTab) {
                    NavTab.SURAH -> SurahList(state.surahs, onSurahClick)
                    NavTab.JUZ -> JuzList(state.juz, onJuzClick)
                    NavTab.PAGE -> PageJump(
                        input = state.jumpPageInput,
                        error = state.jumpPageError,
                        onInputChanged = onJumpInputChanged,
                        onConfirm = onJumpConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahList(surahs: List<Surah>, onClick: (Surah) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(surahs, key = { it.number }) { s ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onClick(s) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "%03d".format(s.number),
                    color = MushafColors.Muted,
                    modifier = Modifier.width(48.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = s.nameAr,
                        fontFamily = MushafArabicFont,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "${s.nameTranslitEn} — ${s.nameTranslationEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MushafColors.Muted,
                    )
                }
                Text(
                    text = "${s.ayahCount} ayahs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MushafColors.Muted,
                )
            }
            HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
        }
    }
}

@Composable
private fun JuzList(juz: List<Juz>, onClick: (Juz) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(juz, key = { it.number }) { j ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onClick(j) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Juz ${j.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(96.dp),
                )
                Text(
                    text = "starts at surah ${j.firstSurah}, ayah ${j.firstAyahInSurah} (page ${j.firstPageNumber})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MushafColors.Muted,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
        }
    }
}

@Composable
private fun PageJump(
    input: String,
    error: String?,
    onInputChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Jump to page (1–604)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = input,
            onValueChange = onInputChanged,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text("Go")
        }
    }
}

private fun NavTab.label(): String = when (this) {
    NavTab.SURAH -> "Surahs"
    NavTab.JUZ -> "Juz"
    NavTab.PAGE -> "Page"
}
