package app.mushaf.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Primary Mushaf font — bundled TTFs referenced from res/font/ (see ADR-0024). */
val MushafArabicFont: FontFamily = FontFamily(
    Font(R.font.uthmanic_hafs_v22, weight = FontWeight.Normal),
)

/** Alternate Mushaf font — Amiri Quran (SIL OFL). Available for future font switching. */
val AmiriQuranFont: FontFamily = FontFamily(
    Font(R.font.amiri_quran, weight = FontWeight.Normal),
)

/** Base Material 3 typography. Reader text uses MushafArabicFont at the call site. */
val MushafTypography = Typography(
    displayLarge  = TextStyle(fontSize = 40.sp),
    displayMedium = TextStyle(fontSize = 32.sp),
    titleLarge    = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium),
    titleMedium   = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge     = TextStyle(fontSize = 16.sp),
    bodyMedium    = TextStyle(fontSize = 14.sp),
    labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium   = TextStyle(fontSize = 12.sp),
    labelSmall    = TextStyle(fontSize = 11.sp),
)
