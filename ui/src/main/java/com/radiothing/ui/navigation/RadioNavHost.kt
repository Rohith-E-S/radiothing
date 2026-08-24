package com.radiothing.ui.navigation

import androidx.compose.runtime.Composable
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

@Composable
fun RadioNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Browse.route,
        modifier = modifier
    ) {
        composable(Screen.Browse.route) {
            BrowseScreen(
                viewModel = hiltViewModel(),
                onStationClick = { navController.navigate(Screen.NowPlaying.route) }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = hiltViewModel(),
                onStationClick = { navController.navigate(Screen.NowPlaying.route) }
            )
        }
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = hiltViewModel()
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = hiltViewModel(),
                onStationClick = { navController.navigate(Screen.NowPlaying.route) }
            )
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                viewModel = hiltViewModel(),
                onPlaylistClick = { id -> navController.navigate("playlist/$id") }
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
                onBackClick = { navController.popBackStack() },
                onStationClick = { navController.navigate(Screen.NowPlaying.route) }
            )
        }
    }
}
