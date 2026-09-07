package app.mushaf.feature.reader

import android.app.Activity
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import app.mushaf.core.domain.model.LineType
import app.mushaf.core.domain.model.MushafFont
import app.mushaf.core.domain.model.Page
import app.mushaf.core.domain.model.PageAyah
import app.mushaf.core.domain.model.PageLine
import app.mushaf.core.domain.model.TokenKind
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

/**
 * Reader architecture (post visual-composition rewrite):
 *
 * ```
 * Column(fillMaxSize) {
 *     ReaderTopBar (AnimatedVisibility, real layout element — never a Z overlay)
 *     HorizontalPager(weight = 1f) {
 *         MushafPage (bounded reading canvas with internal margins + footer)
 *     }
 * }
 * ```
 *
 * The Mushaf page ALWAYS owns its own bounded canvas. Chrome takes real
 * layout space when visible (pushing the page down) and collapses out of
 * layout when hidden (letting the page use the extra space). The result:
 * chrome and Quran text can never occupy the same pixels regardless of
 * device size, orientation, inset behavior, or gesture-nav mode.
 */
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

    // Back button hides controls first; system falls through afterward.
    BackHandler(enabled = state.isControlsVisible) { onEvent(ReaderEvent.HideControls) }

    // System bars visibility mirrors chrome visibility. Immersive when hidden.
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

    // Keep-screen-on scoped to the reader while enabled in settings.
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

    val ctx = LocalContext.current
    val motionScale = remember {
        Settings.Global.getFloat(ctx.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    }

    val a11yDescription = buildString {
        append("Reading page ").append(state.currentPageNumber).append(" of ").append(TOTAL_PAGES).append(". ")
        state.currentSurahNameAr?.let { append("Surah ").append(it).append(". ") }
        state.currentJuzNumber?.let { append("Juz ").append(it).append(". ") }
        append("Swipe left for next page, right for previous. Tap the center to toggle controls.")
    }

    // RTL context for the whole reader. Individual chrome pieces re-anchor
    // to LTR where needed so icon positions stay predictable across locales.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { contentDescription = a11yDescription },
        ) {
            // Chrome zone — collapses out of layout when hidden so the Mushaf
            // canvas expands into the recovered space. Never Z-stacked on
            // top of the pager.
            val enterTransition = if (motionScale == 0f) EnterTransition.None else fadeIn() + expandVertically()
            val exitTransition = if (motionScale == 0f) ExitTransition.None else fadeOut() + shrinkVertically()
            AnimatedVisibility(
                visible = state.isControlsVisible,
                enter = enterTransition,
                exit = exitTransition,
            ) {
                ReaderTopBar(
                    isBookmarked = state.isCurrentPageBookmarked,
                    onOpenNavigation = onOpenNavigation,
                    onOpenSearch = onOpenSearch,
                    onToggleBookmark = { onEvent(ReaderEvent.ToggleBookmark) },
                )
            }

            // Mushaf reading canvas — real weighted layout element.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(0.dp),
            ) { pageIndex ->
                TapZone(onToggle = { onEvent(ReaderEvent.ToggleControls) }) {
                    MushafPage(
                        pageNumber = pageIndex + 1,
                        fontFamily = fontFamily,
                        fontSizeSp = state.settings.fontSizeSp,
                        lineHeightMultiplier = state.settings.lineHeightMultiplier,
                        loadPage = loadPage,
                    )
                }
            }
        }
    }
}

/**
 * Bare-minimum application chrome. Contains only controls (menu, bookmark,
 * search) — the Juz and Surah indicators have moved onto the Mushaf page
 * itself, inside [MushafHeaderBand] (see ADR-0026 rev: Reader visual redesign).
 *
 * Applies its own status-bar inset padding so the underlying Column layout
 * doesn't need to know about system chrome.
 */
@Composable
private fun ReaderTopBar(
    isBookmarked: Boolean,
    onOpenNavigation: () -> Unit,
    onOpenSearch: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenNavigation) {
                Icon(Icons.Filled.Menu, contentDescription = "Navigation")
            }
            Spacer(Modifier.weight(1f))
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

/**
 * Central-band tap detector — placed INSIDE each pager slot so the horizontal
 * drag semantics of the pager are preserved. Excludes the vertical edges so
 * accidental edge taps during a swipe don't fire.
 */
@Composable
private fun TapZone(
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
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

/**
 * A single Mushaf page — the "content layer". Delegates its structure to
 * [MushafPageFrame] which owns the outer/inner border, header and footer
 * bands, and separators. This composable is now responsible only for:
 *
 *  - Loading the page's data (via `loadPage`)
 *  - Choosing what content flows into each of the frame's slots
 *  - Applying the bottom system-inset padding (so the frame footer sits
 *    above the gesture-nav area on modern phones)
 *
 * Juz + Surah live in the frame's header slot (moved off the chrome).
 * Page number lives in the frame's footer slot.
 *
 * Ornamentation of the frame itself is deliberately restrained pending
 * reference-image calibration; see [MushafPageFrame] for the tunables.
 */
@Composable
private fun MushafPage(
    pageNumber: Int,
    fontFamily: FontFamily,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
    loadPage: suspend (Int) -> Page,
) {
    val loadState by produceState<PageLoadState>(initialValue = PageLoadState.Loading, key1 = pageNumber) {
        value = runCatching { loadPage(pageNumber) }
            .fold(
                onSuccess = { PageLoadState.Loaded(it) },
                onFailure = { PageLoadState.Error(it.message ?: it::class.simpleName ?: "unknown") },
            )
    }
    val current: Page = when (val s = loadState) {
        PageLoadState.Loading -> {
            LoadingBox()
            return
        }
        is PageLoadState.Error -> {
            ErrorBox(pageNumber = pageNumber, reason = s.reason)
            return
        }
        is PageLoadState.Loaded -> s.page
    }

    // Derive header labels from the page itself so the frame is self-contained:
    // no chrome/global state is required to fill its slots.
    val primarySurah = current.surahsOnPage.firstOrNull()
    val surahNameAr = primarySurah?.let { s -> current.ayahs.firstOrNull { it.surah == s }?.surahNameAr }
    // Juz number for this page: prefer a juz that starts here, otherwise derive
    // from the current-page juz. Since the domain model already tracks juz on
    // ayahs elsewhere but not on `Page`, we approximate with the juz assigned
    // to the page's first ayah (looked up via the DAO in a future refactor).
    // Frame renders nothing when juzNumber is null.
    val juzNumber: Int? = current.juzStartsOnPage.firstOrNull()

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        MushafPageFrame(
            modifier = Modifier.fillMaxSize(),
            header = { MushafHeaderBand(surahNameAr = surahNameAr, juzNumber = juzNumber) },
            footer = { MushafFooterBand(pageNumber = pageNumber) },
            body = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    MushafPageContent(
                        page = current,
                        fontFamily = fontFamily,
                        fontSizeSp = fontSizeSp,
                        lineHeightMultiplier = lineHeightMultiplier,
                    )
                }
            },
        )
    }
}

/**
 * Renders a page.
 *
 * If the bundled asset carries the ADR-0026 QCF4 line layout (v1.1.0+), we
 * render line-by-line — each physical mushaf line becomes exactly one Text
 * composable so the printed line boundaries are preserved. Ayah markers are
 * inserted inline immediately after the last token of each ayah.
 *
 * Fallback: for v1.0.0 assets that predate the migration, `page.lines` is
 * empty and we fall back to the ayah-flow renderer.
 */
@Composable
private fun MushafPageContent(
    page: Page,
    fontFamily: FontFamily,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
) {
    if (page.lines.isNotEmpty()) {
        val surahNames = remember(page) { page.ayahs.associate { it.surah to it.surahNameAr } }
        Column(Modifier.fillMaxWidth()) {
            for (line in page.lines) {
                when (line.lineType) {
                    LineType.SURAH_NAME -> SurahHeader(
                        name = line.surahNumber?.let { surahNames[it] }
                            ?: page.ayahs.firstOrNull { it.surahStartsHere }?.surahNameAr
                            ?: "",
                    )
                    LineType.BASMALAH -> Bismillah(
                        fontFamily = fontFamily,
                        fontSize = fontSizeSp,
                    )
                    LineType.AYAH -> MushafLine(
                        line = line,
                        fontFamily = fontFamily,
                        fontSizeSp = fontSizeSp,
                        lineHeightMultiplier = lineHeightMultiplier,
                    )
                }
            }
        }
        return
    }

    // Fallback: ayah-flow renderer for v1.0.0 assets.
    val blocks = remember(page) { buildMushafBlocks(page.ayahs) }
    Column(Modifier.fillMaxWidth()) {
        for (block in blocks) {
            when (block) {
                is MushafBlock.SurahHeader -> {
                    SurahHeader(block.name)
                    if (block.showBismillah) {
                        Bismillah(fontFamily = fontFamily, fontSize = fontSizeSp)
                    }
                }
                is MushafBlock.AyahBody -> {
                    AyahBodyText(
                        ayahs = block.ayahs,
                        fontFamily = fontFamily,
                        fontSizeSp = fontSizeSp,
                        lineHeightMultiplier = lineHeightMultiplier,
                    )
                }
            }
        }
    }
}

/**
 * One physical mushaf line rendered as a single Text composable so the
 * printed line boundaries are preserved (Charter #8, ADR-0026). Ayah markers
 * are inlined at each ayah's last-token boundary via [appendAyahMarker].
 */
@Composable
private fun MushafLine(
    line: PageLine,
    fontFamily: FontFamily,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
) {
    val accent = MaterialTheme.colorScheme.primary
    val annotated = remember(line, fontSizeSp, accent) {
        buildAnnotatedString {
            val words = line.words
            for (i in words.indices) {
                val w = words[i]
                append(w.tanzilToken)
                val nextIsSameAyah = i + 1 < words.size &&
                    words[i + 1].surah == w.surah &&
                    words[i + 1].ayah == w.ayah
                if (!nextIsSameAyah && w.kind == TokenKind.WORD) {
                    appendAyahMarker(ayahNumber = w.ayah, accent = accent, fontSizeSp = fontSizeSp)
                } else if (i + 1 < words.size) {
                    append(' ')
                }
            }
        }
    }

    val textAlign = if (line.isCentered) TextAlign.Center else TextAlign.Justify
    Text(
        text = annotated,
        fontFamily = fontFamily,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = textAlign,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { },
    )
}

/**
 * Restrained mushaf-style surah header: centered name flanked by two thin
 * accent-color dividers at half width. No gilded frames, no illumination —
 * elegance via proportion.
 */
@Composable
private fun SurahHeader(name: String) {
    val accent = MaterialTheme.colorScheme.primary
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(
            color = accent.copy(alpha = 0.25f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth(0.5f),
        )
        Text(
            text = name,
            fontFamily = MushafArabicFont,
            fontSize = 24.sp,
            color = accent,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        HorizontalDivider(
            color = accent.copy(alpha = 0.25f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth(0.5f),
        )
    }
}

@Composable
private fun Bismillah(fontFamily: FontFamily, fontSize: Float) {
    Text(
        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
        fontFamily = fontFamily,
        fontSize = (fontSize * 0.95f).sp,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clearAndSetSemantics { },
    )
}

/**
 * Justified ayah body with Uthmani-style verse-end markers (U+06DD ۝ with a
 * small centered Arabic numeral inside). Charter: TalkBack does NOT read the
 * Arabic body — the outer Reader box announces context instead.
 */
@Composable
private fun AyahBodyText(
    ayahs: List<PageAyah>,
    fontFamily: FontFamily,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
) {
    val accent = MaterialTheme.colorScheme.primary
    val body = remember(ayahs, fontSizeSp, accent) {
        buildAnnotatedString {
            for (a in ayahs) {
                append(a.textUthmani)
                appendAyahMarker(ayahNumber = a.ayah, accent = accent, fontSizeSp = fontSizeSp)
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
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { },
    )
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(pageNumber: Int, reason: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Could not load page $pageNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
        )
        Text(
            reason,
            style = MaterialTheme.typography.labelSmall,
            color = MushafColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private sealed interface MushafBlock {
    data class SurahHeader(val name: String, val showBismillah: Boolean) : MushafBlock
    data class AyahBody(val ayahs: List<PageAyah>) : MushafBlock
}

/**
 * Splits a page's ayahs into (SurahHeader?, AyahBody) blocks so headers and
 * the Bismillah can be styled as centered composables rather than inline
 * runs inside a single AnnotatedString. Al-Fatiha's Bismillah is intentionally
 * kept as its first ayah — no extra Bismillah rendered above it.
 */
private fun buildMushafBlocks(ayahs: List<PageAyah>): List<MushafBlock> {
    if (ayahs.isEmpty()) return emptyList()
    val out = mutableListOf<MushafBlock>()
    var buffer = mutableListOf<PageAyah>()
    for (a in ayahs) {
        if (a.surahStartsHere) {
            if (buffer.isNotEmpty()) {
                out += MushafBlock.AyahBody(buffer.toList())
                buffer = mutableListOf()
            }
            out += MushafBlock.SurahHeader(
                name = a.surahNameAr,
                showBismillah = a.bismillahPre && a.surah != 1,
            )
        }
        buffer.add(a)
    }
    if (buffer.isNotEmpty()) {
        out += MushafBlock.AyahBody(buffer.toList())
    }
    return out
}

private sealed interface PageLoadState {
    data object Loading : PageLoadState
    data class Loaded(val page: Page) : PageLoadState
    data class Error(val reason: String) : PageLoadState
}

private fun toArabicNumerals(n: Int): String {
    val map = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return n.toString().map { ch -> map[ch.digitToInt()] }.joinToString("")
}

/**
 * Traditional inline ayah marker — Unicode U+06DD (۝) with the ayah number
 * in Arabic-Indic numerals nested inside, sized ~65 % of the body font.
 *
 * Rendered as an [AnnotatedString] span so it participates in the line's
 * text layout (no floating badge, no Material icon). This is a text-marker,
 * not a UI badge — flip to a rosette-glyph implementation later without
 * changing callers if the reference image calls for it.
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.appendAyahMarker(
    ayahNumber: Int,
    accent: androidx.compose.ui.graphics.Color,
    fontSizeSp: Float,
) {
    withStyle(SpanStyle(color = accent, fontSize = (fontSizeSp * 0.65f).sp)) {
        append(" ۝" + toArabicNumerals(ayahNumber) + " ")
    }
}

private const val TOTAL_PAGES = 604
