package app.mushaf.core.domain.model

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class MushafFont { KFGQPC, AMIRI_QURAN }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val font: MushafFont = MushafFont.KFGQPC,
    val fontSizeSp: Float = 30f,
    val lineHeightMultiplier: Float = 2.2f,
    val keepScreenOn: Boolean = true,
)
