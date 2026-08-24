package com.radiothing.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.Playlist
import com.radiothing.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylistDetails = MutableStateFlow<Playlist?>(null)
    val currentPlaylistDetails: StateFlow<Playlist?> = _currentPlaylistDetails.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            playlistRepository.getPlaylists().collectLatest { list ->
                _playlists.value = list
            }
        }
    }

    fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            try {
                // Observe playlist with stations; take first value for detail header
                playlistRepository.getPlaylistWithStations(id).collectLatest { data ->
                    _currentPlaylistDetails.value = data?.playlist
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try { playlistRepository.createPlaylist(name) } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            try { playlistRepository.deletePlaylist(id) } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun clearError() { _error.value = null }
}
