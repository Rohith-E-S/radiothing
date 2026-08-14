package com.radiothing.ui.navigation

sealed class Screen(val route: String) {
    object Browse : Screen("browse")
    object Favorites : Screen("favorites")
    object Playlists : Screen("playlists")
    data class PlaylistDetail(val playlistId: Long) : Screen("playlist/$playlistId") {
        fun createRoute() = "playlist/$playlistId"
        companion object { const val ROUTE = "playlist/{playlistId}" }
    }
    object NowPlaying : Screen("now_playing")
    object Settings : Screen("settings")
    object History : Screen("history")
}
