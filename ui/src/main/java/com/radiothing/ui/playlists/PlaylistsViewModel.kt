package com.radiothing.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.RadioStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistsViewModel : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylistDetails = MutableStateFlow<Playlist?>(null)
    val currentPlaylistDetails: StateFlow<Playlist?> = _currentPlaylistDetails.asStateFlow()

    init {
        // Initialize or fetch playlists here
    }

    fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            // Load playlist details with stations
        }
    }
}
