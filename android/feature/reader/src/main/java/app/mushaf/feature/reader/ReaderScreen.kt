package app.mushaf.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mushaf.core.designsystem.AmiriQuranFont
import app.mushaf.core.designsystem.MushafArabicFont
import app.mushaf.core.designsystem.MushafColors
import app.mushaf.core.domain.model.MushafFont
import app.mushaf.core.domain.model.Page
import kotlinx.coroutines.flow.distinctUntilChanged

/** Route entry — Hilt-injected VM. */
@Composable
fun ReaderRoute(
    onOpenNavigation: () -> Unit,
    initialPage: Int? = null,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialPage) {
        if (initialPage != null) viewModel.onEvent(ReaderEvent.JumpToPage(initialPage))
    }

    ReaderContent(
        state = state,
        loadPage = { viewModel.loadPage(it) },
        onEvent = viewModel::onEvent,
        onOpenNavigation = onOpenNavigation,
    )
}

@Composable
fun ReaderContent(
    state: ReaderUiState,
    loadPage: suspend (Int) -> Page,
    onEvent: (ReaderEvent) -> Unit,
    onOpenNavigation: () -> Unit,
) {
    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentPageNumber - 1,
        pageCount = { TOTAL_PAGES },
    )

    // Sync pager <-> ViewModel state changes.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { pageIndex ->
                onEvent(ReaderEvent.PageChanged(pageIndex + 1))
            }
    }
    LaunchedEffect(state.currentPageNumber) {
        val target = state.currentPageNumber - 1
        if (pagerState.currentPage != target && pagerState.settledPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    val fontFamily: FontFamily = when (state.settings.font) {
        MushafFont.KFGQPC -> MushafArabicFont
        MushafFont.AMIRI_QURAN -> AmiriQuranFont
    }

    // Reader: swiping in RTL — visual left = next page.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
            ) { pageIndex ->
                PageRenderer(
                    pageNumber = pageIndex + 1,
                    fontFamily = fontFamily,
                    fontSizeSp = state.settings.fontSizeSp,
                    lineHeightMultiplier = state.settings.lineHeightMultiplier,
                    loadPage = loadPage,
                )
            }
            TopChrome(
                currentPage = state.currentPageNumber,
                onOpenNavigation = onOpenNavigation,
            )
        }
    }
}

@Composable
private fun TopChrome(
    currentPage: Int,
    onOpenNavigation: () -> Unit,
) {
    // Simple, quiet top chrome: menu button + page counter. Charter — Controls > Decoration.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onOpenNavigation, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Filled.Menu, contentDescription = "Navigation")
            }
            Text(
                text = "Page $currentPage / $TOTAL_PAGES",
                style = MaterialTheme.typography.labelMedium,
                color = MushafColors.Muted,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun PageRenderer(
    pageNumber: Int,
    fontFamily: FontFamily,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
    loadPage: suspend (Int) -> Page,
) {
    val page by produceState<Page?>(initialValue = null, key1 = pageNumber) {
        value = loadPage(pageNumber)
    }
    val current = page
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val accent = MushafColors.Accent
        val body = buildAnnotatedString {
            var seenSurah = -1
            for (a in current.ayahs) {
                if (a.surahStartsHere && a.surah != seenSurah) {
                    withStyle(SpanStyle(color = accent, fontSize = (fontSizeSp * 1.1f).sp)) {
                        append("\n${a.surahNameAr}\n")
                    }
                    if (a.bismillahPre && a.surah != 1) {
                        append("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ\n")
                    }
                    seenSurah = a.surah
                }
                append(a.textUthmani)
                withStyle(SpanStyle(color = accent, fontSize = (fontSizeSp * 0.7f).sp)) {
                    append(" ۝${toArabicNumerals(a.ayah)} ")
                }
            }
        }
        Text(
            text = body,
            fontFamily = fontFamily,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun toArabicNumerals(n: Int): String {
    val map = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return n.toString().map { ch -> map[ch.digitToInt()] }.joinToString("")
}

private const val TOTAL_PAGES = 604
