package app.mushaf.prototype

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val KfgqpcFontFamily = FontFamily(
    Font("fonts/uthmanic_hafs_v22.ttf", weight = FontWeight.Normal),
)
private val AmiriQuranFontFamily = FontFamily(
    Font("fonts/amiri_quran.ttf", weight = FontWeight.Normal),
)

@Composable
fun ReaderScreen(quran: Quran) {
    // Reader state — kept minimal for the prototype (no ViewModel, no
    // process-death restoration; those come in Phase 5).
    var useKfgqpc by remember { mutableStateOf(true) }
    var fontSize by remember { mutableFloatStateOf(30f) }
    var lineHeight by remember { mutableFloatStateOf(2.2f) }

    // 604 mushaf pages; Compose Pager wraps our 1-indexed page numbers.
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { Quran.TOTAL_PAGES },
    )

    Column(Modifier.fillMaxSize()) {
        Controls(
            useKfgqpc = useKfgqpc,
            onFontToggle = { useKfgqpc = !useKfgqpc },
            fontSize = fontSize,
            onFontSize = { fontSize = it },
            lineHeight = lineHeight,
            onLineHeight = { lineHeight = it },
            currentPage = pagerState.currentPage + 1,
        )

        // RTL for Arabic swipe direction — next page is to the LEFT visually,
        // which HorizontalPager honors when LayoutDirection is Rtl.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
            ) { pageIndex ->
                val pageNumber = pageIndex + 1
                val pageData = remember(pageNumber) { quran.getPage(pageNumber) }
                MushafPage(
                    page = pageData,
                    fontFamily = if (useKfgqpc) KfgqpcFontFamily else AmiriQuranFontFamily,
                    fontSizeSp = fontSize,
                    lineHeightMultiplier = lineHeight,
                )
            }
        }
    }
}

@Composable
private fun Controls(
    useKfgqpc: Boolean,
    onFontToggle: () -> Unit,
    fontSize: Float,
    onFontSize: (Float) -> Unit,
    lineHeight: Float,
    onLineHeight: (Float) -> Unit,
    currentPage: Int,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDE7D8))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Page $currentPage / ${Quran.TOTAL_PAGES}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF3A3A3A),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onFontToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7A5A2E),
                    ),
                ) {
                    Text(if (useKfgqpc) "KFGQPC" else "Amiri Quran", fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Size ${fontSize.toInt()}", modifier = Modifier.width(64.dp), fontSize = 12.sp)
                Slider(
                    value = fontSize,
                    onValueChange = onFontSize,
                    valueRange = 20f..50f,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Line ${"%.1f".format(lineHeight)}", modifier = Modifier.width(64.dp), fontSize = 12.sp)
                Slider(
                    value = lineHeight,
                    onValueChange = onLineHeight,
                    valueRange = 1.5f..3.5f,
                    steps = 19,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MushafPage(
    page: PageData,
    fontFamily: FontFamily,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
) {
    val paperColor = Color(0xFFFBF7EC)
    val accent = Color(0xFF7A5A2E)
    val ink = Color(0xFF1E1E1E)

    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .background(paperColor)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        // Surah headings + inline bismillah when a surah starts on this page.
        var previousSurah = -1
        val body = buildAnnotatedString {
            for (a in page.ayahs) {
                val newSurah = a.surah != previousSurah
                previousSurah = a.surah
                if (newSurah && a.ayah == 1) {
                    withStyle(SpanStyle(color = accent, fontSize = fontSizeSp.sp)) {
                        append("\n${a.surahNameAr}\n")
                    }
                    if (a.bismillahPre && a.surah != 1) {
                        append("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ\n")
                    }
                }
                append(a.text)
                withStyle(SpanStyle(color = accent, fontSize = (fontSizeSp * 0.75f).sp)) {
                    append(" ۝${toArabicNumerals(a.ayah)} ")
                }
            }
        }

        Text(
            text = body,
            fontFamily = fontFamily,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
            color = ink,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun toArabicNumerals(n: Int): String {
    val map = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return n.toString().map { ch -> map[ch.digitToInt()] }.joinToString("")
}
