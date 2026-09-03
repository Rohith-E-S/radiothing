package com.radiothing.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.radiothing.ui.browse.BrowseScreen
import com.radiothing.ui.favorites.FavoritesScreen
import com.radiothing.ui.history.HistoryScreen
import com.radiothing.ui.nowplaying.NowPlayingScreen
import com.radiothing.ui.playlists.PlaylistDetailScreen
import com.radiothing.ui.playlists.PlaylistsScreen
import com.radiothing.ui.settings.SettingsScreen

val TOP_LEVEL_TABS: List<Screen> = listOf(
    Screen.Browse,
    Screen.Favorites,
    Screen.Playlists,
    Screen.History,
    Screen.Settings,
)

/**
 * Placeholder start destination. The NavHost must be composed from first frame
 * so the NavController's graph is set — navigating into an overlay route before
 * any NavHost composition throws (route not found in the empty graph) and
 * crashes the app. This destination renders nothing and lets input fall
 * through to the tab pager beneath it.
 */
private const val OVERLAY_ROOT = "overlay_root"

/**
 * Nav host for drilled-in destinations (NowPlaying, PlaylistDetail). Composed
 * unconditionally by [MainActivity]; the pager manages tab state, this host
 * only handles overlay screens entered via navigate().
 */
@Composable
fun RadioNavHost(
    navController: NavHostController,
    playerManager: com.radiothing.player.PlayerManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = OVERLAY_ROOT,
        modifier = modifier
    ) {
        // Transparent anchor — no UI, no input handling
        composable(OVERLAY_ROOT) { }
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.PlaylistDetail.ROUTE,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            PlaylistDetailScreen(
                playlistId = id,
                viewModel = hiltViewModel(),
                playerManager = playerManager,
                onBackClick = { navController.popBackStack() },
                onStationClick = {
                    navController.navigate(Screen.NowPlaying.route) { launchSingleTop = true }
                }
            )
        }
    }
}

/**
 * Horizontal pager hosting the 5 top-level tab screens. Reused across tab
 * changes — pager state is owned by [MainActivity] so the screens and scroll
 * positions survive tab switches.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    playerManager: com.radiothing.player.PlayerManager,
    onStationClick: (String) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = true,
        // 0 keeps off-screen pages from running InfiniteTransitions/Canvas
        // at 120Hz while still preserving scroll position via pagerState.
        beyondBoundsPageCount = 0,
        key = { idx -> TOP_LEVEL_TABS[idx].route }
    ) { page ->
        when (TOP_LEVEL_TABS[page]) {
            Screen.Browse -> BrowseScreen(
                viewModel = hiltViewModel(),
                playerManager = playerManager,
                onStationClick = onStationClick
            )
            Screen.Favorites -> FavoritesScreen(
                viewModel = hiltViewModel(),
                playerManager = playerManager,
                onStationClick = onStationClick
            )
            Screen.Playlists -> PlaylistsScreen(
                viewModel = hiltViewModel(),
                onPlaylistClick = onPlaylistClick
            )
            Screen.History -> HistoryScreen(
                viewModel = hiltViewModel(),
                playerManager = playerManager,
                onStationClick = onStationClick
            )
            Screen.Settings -> SettingsScreen(
                viewModel = hiltViewModel()
            )
            else -> { /* NowPlaying / PlaylistDetail are not top-level tabs */ }
        }
    }
}
