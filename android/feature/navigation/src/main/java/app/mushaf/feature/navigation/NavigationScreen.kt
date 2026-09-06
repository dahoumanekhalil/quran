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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mushaf.core.designsystem.MushafArabicFont
import app.mushaf.core.designsystem.MushafColors
import app.mushaf.core.domain.model.Bookmark
import app.mushaf.core.domain.model.Juz
import app.mushaf.core.domain.model.Surah
import java.text.DateFormat
import java.util.Date

@Composable
fun NavigationRoute(
    onJumpToPage: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavigationContent(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onSurahClick = { surah -> viewModel.jumpToSurah(surah.number) { onClose() } },
        onJuzClick = { juz -> viewModel.jumpToJuz(juz.number) { onClose() } },
        onBookmarkClick = { bm -> viewModel.jumpToPage(bm.pageNumber) { onClose() } },
        onBookmarkRemove = viewModel::removeBookmark,
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
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun NavigationContent(
    state: NavigationUiState,
    onTabSelected: (NavTab) -> Unit,
    onSurahClick: (Surah) -> Unit,
    onJuzClick: (Juz) -> Unit,
    onBookmarkClick: (Bookmark) -> Unit,
    onBookmarkRemove: (Int) -> Unit,
    onJumpInputChanged: (String) -> Unit,
    onJumpConfirm: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Box(Modifier.fillMaxWidth()) {
            Text(
                "Navigation",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
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
                    NavTab.BOOKMARKS -> BookmarkList(
                        bookmarks = state.bookmarks,
                        onClick = onBookmarkClick,
                        onRemove = onBookmarkRemove,
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
                    .clickable(onClickLabel = "Open ${s.nameTranslitEn}") { onClick(s) }
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
                    .clickable(onClickLabel = "Open Juz ${j.number}") { onClick(j) }
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

@Composable
private fun BookmarkList(
    bookmarks: List<Bookmark>,
    onClick: (Bookmark) -> Unit,
    onRemove: (Int) -> Unit,
) {
    if (bookmarks.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "No bookmarks yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MushafColors.Muted,
                textAlign = TextAlign.Center,
            )
            Text(
                "Tap the ribbon icon on any page to save it.",
                style = MaterialTheme.typography.labelSmall,
                color = MushafColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        return
    }

    val dateFormat = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(bookmarks, key = { "${it.pageNumber}:${it.ayahGlobalIndex ?: 0}" }) { bm ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = "Open page ${bm.pageNumber}") { onClick(bm) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Page ${bm.pageNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Saved ${dateFormat.format(Date(bm.createdAtEpochMillis))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MushafColors.Muted,
                    )
                }
                IconButton(onClick = { onRemove(bm.pageNumber) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove bookmark")
                }
            }
            HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
        }
    }
}

private fun NavTab.label(): String = when (this) {
    NavTab.SURAH -> "Surahs"
    NavTab.JUZ -> "Juz"
    NavTab.PAGE -> "Page"
    NavTab.BOOKMARKS -> "Saved"
}
