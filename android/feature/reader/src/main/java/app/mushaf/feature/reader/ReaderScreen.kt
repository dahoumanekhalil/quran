package app.mushaf.feature.reader

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    onOpenSearch: () -> Unit,
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
        onOpenSearch = onOpenSearch,
    )
}

@Composable
fun ReaderContent(
    state: ReaderUiState,
    loadPage: suspend (Int) -> Page,
    onEvent: (ReaderEvent) -> Unit,
    onOpenNavigation: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Back button hides controls first; system falls through to default nav after that.
    BackHandler(enabled = state.isControlsVisible) { onEvent(ReaderEvent.HideControls) }

    // Immersive mode (TASK-075): hide system bars when controls hidden.
    // Restore bars on leaving the reader so other screens aren't stuck immersive.
    val view = LocalView.current
    DisposableEffect(view, state.isControlsVisible) {
        val window = (view.context as? Activity)?.window
        val insets = window?.let { WindowCompat.getInsetsController(it, view) }
        if (insets != null) {
            insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (state.isControlsVisible) {
                insets.show(WindowInsetsCompat.Type.systemBars())
            } else {
                insets.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            insets?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Keep-screen-on setting (part of TASK-075): only while the reader is on screen.
    DisposableEffect(view, state.settings.keepScreenOn) {
        val window = (view.context as? Activity)?.window
        if (window != null && state.settings.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentPageNumber - 1,
        pageCount = { TOTAL_PAGES },
    )

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
                // Tap-to-toggle (TASK-074) lives inside each page so the pager
                // still owns horizontal drags. Center 60% band avoids edge conflicts.
                TapZone(onToggle = { onEvent(ReaderEvent.ToggleControls) }) {
                    PageRenderer(
                        pageNumber = pageIndex + 1,
                        fontFamily = fontFamily,
                        fontSizeSp = state.settings.fontSizeSp,
                        lineHeightMultiplier = state.settings.lineHeightMultiplier,
                        loadPage = loadPage,
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                TopChrome(
                    currentPage = state.currentPageNumber,
                    surahNameAr = state.currentSurahNameAr,
                    juzNumber = state.currentJuzNumber,
                    isBookmarked = state.isCurrentPageBookmarked,
                    onOpenNavigation = onOpenNavigation,
                    onOpenSearch = onOpenSearch,
                    onToggleBookmark = { onEvent(ReaderEvent.ToggleBookmark) },
                )
            }
        }
    }
}

@Composable
private fun TapZone(
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Central 60% vertical + 100% horizontal band — matches TASK-074 spec.
    // Wraps `content` so the pager's swipe still reaches the underlying page.
    BoxWithConstraints(modifier.fillMaxSize()) {
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val topGuard = heightPx * 0.2f
        val bottomGuard = heightPx * 0.8f
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset: Offset ->
                            if (offset.y in topGuard..bottomGuard) onToggle()
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun TopChrome(
    currentPage: Int,
    surahNameAr: String?,
    juzNumber: Int?,
    isBookmarked: Boolean,
    onOpenNavigation: () -> Unit,
    onOpenSearch: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    // Quiet, two-line reading-context strip: Arabic surah name up top, then the
    // page/juz reference below. Charter — Controls > Decoration.
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.align(Alignment.Center),
            ) {
                if (!surahNameAr.isNullOrBlank()) {
                    Text(
                        text = surahNameAr,
                        fontFamily = MushafArabicFont,
                        fontSize = 16.sp,
                        color = MushafColors.Muted,
                    )
                }
                Text(
                    text = buildString {
                        if (juzNumber != null) append("Juz ").append(juzNumber).append(" • ")
                        append("Page ").append(currentPage).append(" / ").append(TOTAL_PAGES)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MushafColors.Muted,
                )
            }
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                    )
                }
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
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
