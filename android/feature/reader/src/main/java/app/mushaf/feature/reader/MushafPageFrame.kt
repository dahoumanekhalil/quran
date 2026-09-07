package app.mushaf.feature.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mushaf.core.designsystem.MushafArabicFont
import app.mushaf.core.designsystem.MushafColors

/**
 * Structural skeleton of a printed Mushaf page.
 *
 * Composition (top → bottom):
 *
 * ```
 * ┌─────────────────────────────────────┐   ← outer border
 * │ ┌─────────────────────────────────┐ │   ← inner border
 * │ │  header  (Juz | Surah)          │ │
 * │ │  ─────────────                  │ │   ← header/body separator
 * │ │                                 │ │
 * │ │  body (Quran text region)       │ │
 * │ │                                 │ │
 * │ │  ─────────────                  │ │   ← body/footer separator
 * │ │  footer (page number)           │ │
 * │ └─────────────────────────────────┘ │
 * └─────────────────────────────────────┘
 * ```
 *
 * Proportions are **fractions of the frame**, not absolute pixels — the frame
 * scales with whatever space the parent gives it, from a narrow 5.5" phone to
 * a small tablet in the same aspect.
 *
 * Ornamentation is intentionally restrained (single-line borders, hairline
 * separators) pending calibration against the reference image. This
 * composable is the composition contract; the aesthetic is a placeholder.
 */
@Composable
fun MushafPageFrame(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    // Theme-driven so the frame reads correctly in both light (warm brown) and
    // dark (lighter brown, MushafColors.AccentDark) modes.
    val accent = MaterialTheme.colorScheme.primary
    val outer = accent.copy(alpha = 0.55f)
    val inner = accent.copy(alpha = 0.30f)
    val separator = accent.copy(alpha = 0.20f)

    BoxWithConstraints(modifier) {
        val h = maxHeight

        // Outer + inner margins as proportions of the frame's height so the
        // composition holds shape from small phone to small tablet.
        val outerMargin: Dp = h * OUTER_MARGIN_FRACTION
        val borderGap: Dp = h * BORDER_GAP_FRACTION
        val innerPadding: Dp = h * INNER_PADDING_FRACTION
        val bandVerticalPadding: Dp = h * BAND_VERTICAL_PADDING_FRACTION

        // Outer border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerMargin)
                .clip(RoundedCornerShape(0.dp))
                .background(MaterialTheme.colorScheme.background),
        ) {
            OutlinedFrame(color = outer, strokeWidth = 1.5.dp) {
                // Inner border (visually a second thin rectangle nested inside the outer one)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(borderGap),
                ) {
                    OutlinedFrame(color = inner, strokeWidth = 0.75.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        ) {
                            // Header band
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = bandVerticalPadding),
                                contentAlignment = Alignment.Center,
                            ) { header() }

                            HorizontalDivider(color = separator, thickness = 0.5.dp)

                            // Body — takes the remaining vertical space
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            ) { body() }

                            HorizontalDivider(color = separator, thickness = 0.5.dp)

                            // Footer band
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = bandVerticalPadding),
                                contentAlignment = Alignment.Center,
                            ) { footer() }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple outlined rectangle wrapper. Not a Card, not a Surface — just a
 * hairline border. Kept private so the aesthetic can be swapped later
 * (SVG-based ornamented border, per-corner motifs, etc.) without changing
 * MushafPageFrame's public shape.
 */
@Composable
private fun OutlinedFrame(
    color: Color,
    strokeWidth: Dp,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Surface(
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        border = BorderStroke(strokeWidth, color),
        modifier = Modifier.fillMaxSize(),
    ) {
        content()
    }
}

/**
 * Mushaf header band content — "Juz N · Surah name" in restrained,
 * printed-mushaf style rather than a Material toolbar.
 *
 * Rendering: two anchored labels (Juz on one side, Surah on the other)
 * with the traditional Arabic word for each. Muted color; sits inside
 * MushafPageFrame's header slot.
 */
@Composable
fun MushafHeaderBand(
    surahNameAr: String?,
    juzNumber: Int?,
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left slot: Juz label
        if (juzNumber != null) {
            Text(
                text = "الجزء " + toArabicNumeralsFrame(juzNumber),
                fontFamily = MushafArabicFont,
                fontSize = 15.sp,
                color = MushafColors.Muted,
            )
        } else {
            Spacer(Modifier.width(1.dp))
        }
        // Right slot: Surah name
        if (!surahNameAr.isNullOrBlank()) {
            Text(
                text = surahNameAr,
                fontFamily = MushafArabicFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = accent,
            )
        } else {
            Spacer(Modifier.width(1.dp))
        }
    }
}

/**
 * Mushaf footer band content — page number in Arabic numerals sitting
 * inside a restrained cartouche (rounded rectangle outline).
 *
 * There is no Tajweed legend (out of scope, ADR-0026). This band is
 * intentionally simple — just the traditional page-number treatment.
 */
@Composable
fun MushafFooterBand(pageNumber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageNumberCartouche(pageNumber = pageNumber)
    }
}

/**
 * The visible page number as it appears on the physical Mushaf page —
 * not "52 / 604" toolbar text. Arabic-Indic numerals inside a hairline
 * cartouche.
 */
@Composable
private fun PageNumberCartouche(pageNumber: Int) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Surface(
            color = Color.Transparent,
            border = BorderStroke(0.75.dp, accent.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(6.dp),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = toArabicNumeralsFrame(pageNumber),
                    fontFamily = MushafArabicFont,
                    fontSize = 13.sp,
                    color = accent,
                )
            }
        }
    }
}

// ---- Layout tuning constants (proportional; awaiting reference-image calibration) ----

private const val OUTER_MARGIN_FRACTION = 0.018f          // ~1.8 % of frame height
private const val BORDER_GAP_FRACTION = 0.008f            // ~0.8 % — space between outer + inner border
private const val INNER_PADDING_FRACTION = 0.014f         // ~1.4 % — inner-border to content
private const val BAND_VERTICAL_PADDING_FRACTION = 0.008f // ~0.8 % — vertical padding in header + footer bands

// Local numeral helper — kept private to this file so it stays independent of
// ReaderScreen's copy. Consolidation into a shared util can happen later.
private fun toArabicNumeralsFrame(n: Int): String {
    val map = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return n.toString().map { ch -> map[ch.digitToInt()] }.joinToString("")
}
