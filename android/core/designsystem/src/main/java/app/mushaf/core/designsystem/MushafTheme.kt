package app.mushaf.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Mushaf-appropriate warm-neutral palette. Not Material dynamic colors. */
object MushafColors {
    val PaperLight = Color(0xFFFBF7EC)
    val PaperDark  = Color(0xFF141210)
    val InkLight   = Color(0xFF1E1E1E)
    val InkDark    = Color(0xFFEDE7D8)
    val Accent     = Color(0xFF7A5A2E)   // Ayah numbers, headings, dividers (light mode)
    val AccentDark = Color(0xFFB89466)   // Same role for dark mode — brighter for legibility on PaperDark
    val Muted      = Color(0xFF8A8266)
    val ControlBg  = Color(0xFFEDE7D8)
    val ControlBgDark = Color(0xFF23201B)
}

private val LightScheme = lightColorScheme(
    primary        = MushafColors.Accent,
    onPrimary      = MushafColors.PaperLight,
    background     = MushafColors.PaperLight,
    onBackground   = MushafColors.InkLight,
    surface        = MushafColors.PaperLight,
    onSurface      = MushafColors.InkLight,
    surfaceVariant = MushafColors.ControlBg,
    onSurfaceVariant = MushafColors.InkLight,
    outline        = MushafColors.Muted,
)

private val DarkScheme = darkColorScheme(
    primary        = MushafColors.AccentDark,
    onPrimary      = MushafColors.PaperDark,
    background     = MushafColors.PaperDark,
    onBackground   = MushafColors.InkDark,
    surface        = MushafColors.PaperDark,
    onSurface      = MushafColors.InkDark,
    surfaceVariant = MushafColors.ControlBgDark,
    onSurfaceVariant = MushafColors.InkDark,
    outline        = MushafColors.Muted,
)

@Composable
fun MushafTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography  = MushafTypography,
        content     = content,
    )
}
