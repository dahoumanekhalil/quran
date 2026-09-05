package app.mushaf.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.mushaf.BuildConfig
import app.mushaf.feature.navigation.NavigationRoute
import app.mushaf.feature.reader.ReaderRoute
import app.mushaf.feature.search.SearchRoute
import app.mushaf.feature.settings.AboutRoute
import app.mushaf.feature.settings.SettingsRoute

/**
 * Root navigation graph. Five destinations for the MVP:
 *  - Reader (default landing).
 *  - Navigation (surah / juz / page-jump / saved).
 *  - Search — FTS5 over the frozen Uthmani corpus.
 *  - Settings — theme, size, font, keep-screen-on.
 *  - About — version, dataset hash, credits, privacy statement.
 *
 * Cross-screen page-jump: NavigationRoute and SearchRoute write the target
 * page to the ReadingPositionRepository directly. Reader observes reading-
 * position changes and syncs the pager.
 */
@Composable
fun MushafNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = READER_ROUTE,
    ) {
        composable(READER_ROUTE) {
            ReaderRoute(
                onOpenNavigation = { nav.navigate(NAVIGATION_ROUTE) },
                onOpenSearch = { nav.navigate(SEARCH_ROUTE) },
                initialPage = null,
            )
        }

        composable(NAVIGATION_ROUTE) {
            NavigationRoute(
                onJumpToPage = { /* handled via ReadingPositionRepository side-effect */ },
                onOpenSettings = { nav.navigate(SETTINGS_ROUTE) },
                onClose = { nav.popBackStack() },
            )
        }

        composable(SEARCH_ROUTE) {
            SearchRoute(onClose = { nav.popBackStack() })
        }

        composable(SETTINGS_ROUTE) {
            SettingsRoute(onOpenAbout = { nav.navigate(ABOUT_ROUTE) })
        }

        composable(ABOUT_ROUTE) {
            AboutRoute(
                appVersionName = BuildConfig.VERSION_NAME,
                appVersionCode = BuildConfig.VERSION_CODE,
                onClose = { nav.popBackStack() },
            )
        }
    }
}

private const val READER_ROUTE = "reader"
private const val NAVIGATION_ROUTE = "navigation"
private const val SEARCH_ROUTE = "search"
private const val SETTINGS_ROUTE = "settings"
private const val ABOUT_ROUTE = "about"
