package app.mushaf.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.mushaf.feature.navigation.NavigationRoute
import app.mushaf.feature.reader.ReaderRoute

/**
 * Root navigation graph. Two destinations for the MVP:
 *  - Reader (default landing; charter — "immediately return to the Quran at
 *    their last reading position").
 *  - Navigation (surah / juz / page-jump). Opened from the Reader's menu.
 *
 * Cross-screen page-jump: NavigationRoute writes the target page to the
 * ReadingPositionRepository directly. Reader observes reading-position
 * changes and syncs the pager. This keeps navigation as pure UI routing
 * and avoids stringly-typed nav arguments.
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
                initialPage = null,
            )
        }

        composable(NAVIGATION_ROUTE) {
            NavigationRoute(
                onJumpToPage = { /* handled by NavigationViewModel side-effect via repository */ },
                onClose = { nav.popBackStack() },
            )
        }
    }
}

private const val READER_ROUTE = "reader"
private const val NAVIGATION_ROUTE = "navigation"
