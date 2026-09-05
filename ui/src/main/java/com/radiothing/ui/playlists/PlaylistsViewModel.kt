package com.radiothing.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.PlaylistWithStations
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.PlaylistRepository
import com.radiothing.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    /** playlistId → station count, for the list rows. */
    val playlistCounts: StateFlow<Map<Long, Int>> = playlistRepository.getPlaylistStationCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _currentPlaylistDetails = MutableStateFlow<PlaylistWithStations?>(null)
    val currentPlaylistDetails: StateFlow<PlaylistWithStations?> = _currentPlaylistDetails.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Station most recently "sent" to the playlist picker (add-to-playlist flow). */
    private val _pendingStation = MutableStateFlow<RadioStation?>(null)
    val pendingStation: StateFlow<RadioStation?> = _pendingStation.asStateFlow()

    /**
     * Single source for the detail screen: switching the id swaps the upstream
     * flow via flatMapLatest, so exactly one Room collector is active at a time.
     * (A collector per loadPlaylist() call leaked: after navigating A → B, any
     * DB change re-emitted from A's still-live flow and overwrote the screen.)
     */
    private val selectedPlaylistId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            playlistRepository.getPlaylists().collectLatest { list ->
                _playlists.value = list
            }
        }
        viewModelScope.launch {
            selectedPlaylistId
                .flatMapLatest { id ->
                    if (id == null) flowOf(null)
                    else playlistRepository.getPlaylistWithStations(id)
                }
                .collectLatest { data ->
                    _currentPlaylistDetails.value = data
                }
        }
    }

    fun loadPlaylist(id: Long) {
        selectedPlaylistId.value = id
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

    /**
     * Toggle a station's favorite flag. The detail flow re-emits with fresh
     * isFavorite values because it combines the favorites table.
     */
    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            try { toggleFavoriteUseCase(station) } catch (e: Exception) { _error.value = e.message }
        }
    }

    // --- Add-to-playlist flow ---

    fun setPendingStation(station: RadioStation) {
        _pendingStation.value = station
    }

    fun clearPendingStation() {
        _pendingStation.value = null
    }

    fun addPendingStationTo(playlistId: Long) {
        val station = _pendingStation.value ?: return
        viewModelScope.launch {
            try { playlistRepository.addStationToPlaylist(playlistId, station) } catch (e: Exception) { _error.value = e.message }
            _pendingStation.value = null
        }
    }

    fun removeStationFromPlaylist(playlistId: Long, stationUuid: String) {
        viewModelScope.launch {
            try { playlistRepository.removeStationFromPlaylist(playlistId, stationUuid) } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun clearError() { _error.value = null }
}
