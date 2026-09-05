package app.mushaf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mushaf.core.designsystem.MushafTheme
import app.mushaf.core.domain.model.ThemeMode
import app.mushaf.navigation.MushafNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MushafRootViewModel = hiltViewModel()
            val mode by vm.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            MushafTheme(darkTheme = darkTheme) {
                MushafNavHost()
            }
        }
    }
}
