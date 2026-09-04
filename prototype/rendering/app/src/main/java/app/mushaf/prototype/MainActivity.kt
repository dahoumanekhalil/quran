package app.mushaf.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = MushafColors) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val quran = remember { Quran(this) }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(PaperColor)
                            .systemBarsPadding(),
                    ) {
                        ReaderScreen(quran = quran)
                    }
                }
            }
        }
    }
}

// Simple palette — mushaf-inspired warm neutrals.
private val PaperColor = Color(0xFFFBF7EC)
private val InkColor = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF7A5A2E)

private val MushafColors = lightColorScheme(
    primary = AccentColor,
    background = PaperColor,
    surface = PaperColor,
    onBackground = InkColor,
    onSurface = InkColor,
)
