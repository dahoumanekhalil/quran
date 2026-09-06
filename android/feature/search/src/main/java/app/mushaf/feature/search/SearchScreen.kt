package app.mushaf.feature.search

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mushaf.core.designsystem.MushafArabicFont
import app.mushaf.core.designsystem.MushafColors
import app.mushaf.core.domain.search.SearchHit

@Composable
fun SearchRoute(
    onClose: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchContent(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onClearQuery = viewModel::clearQuery,
        onResultClick = { hit -> viewModel.openResult(hit) { onClose() } },
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onResultClick: (SearchHit) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Text(
            "Search",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            textAlign = TextAlign.Center,
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            singleLine = true,
            label = { Text("Search the Mushaf") },
            placeholder = { Text("ابحث في المصحف") },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )

        // Sub-100ms searches usually finish before the indicator has a chance to show;
        // it's only visible on longer queries or slow devices.
        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Box(Modifier.fillMaxSize()) {
            when {
                state.showError -> ErrorState(state.error!!)
                state.showHint -> HintState()
                state.showNoResults -> NoResultsState(state.query)
                else -> ResultsList(state.results, onResultClick)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Search failed",
            style = MaterialTheme.typography.bodyMedium,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
        )
        Text(
            message,
            style = MaterialTheme.typography.labelSmall,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun HintState() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Type Arabic to search the Mushaf",
            style = MaterialTheme.typography.bodyMedium,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
        )
        Text(
            "Diacritics and alef variants are optional",
            style = MaterialTheme.typography.labelSmall,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun NoResultsState(query: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "No matches for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultsList(
    hits: List<SearchHit>,
    onClick: (SearchHit) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(hits, key = { it.ayah.globalIndex }) { hit ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = "Open page ${hit.ayah.page}, ayah ${hit.ayah.surah}:${hit.ayah.ayah}") { onClick(hit) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hit.surahNameAr,
                        fontFamily = MushafArabicFont,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = "${hit.ayah.surah}:${hit.ayah.ayah} • Page ${hit.ayah.page} • Juz ${hit.ayah.juz}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MushafColors.Muted,
                    )
                }
                Text(
                    text = hit.snippet,
                    fontFamily = MushafArabicFont,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
            }
            HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
        }
    }
}
