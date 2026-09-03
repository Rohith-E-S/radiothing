package com.radiothing.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.PlayerState
import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.FavoriteRepository
import com.radiothing.domain.repository.PlaylistRepository
import com.radiothing.domain.usecase.ToggleFavoriteUseCase
import com.radiothing.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NowPlayingUiState(
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val volume: Float = 0.5f,
    val error: String? = null,
    val queue: List<RadioStation> = emptyList(),
    val queueIndex: Int = -1,
    val sleepRemainingMs: Long = 0L
)

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())

    val audioSessionId: StateFlow<Int> = playerManager.audioSessionId

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistCounts: StateFlow<Map<Long, Int>> = playlistRepository.getPlaylistStationCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<NowPlayingUiState> = combine(
        _uiState,
        playerManager.playerState,
        playerManager.volume,
        playerManager.sleepTimerRemaining,
        favoriteRepository.getFavoriteIds()
    ) { state, player, volume, sleepMs, favIds ->
        val enrichedStation = player.currentStation?.let { it.copy(isFavorite = favIds.contains(it.stationUuid)) }
        val enrichedQueue = player.queue.map { it.copy(isFavorite = favIds.contains(it.stationUuid)) }
        state.copy(
            currentStation = enrichedStation,
            isPlaying = player.isPlaying,
            isBuffering = player.isBuffering,
            volume = volume,
            error = player.error,
            queue = enrichedQueue,
            queueIndex = player.queueIndex,
            sleepRemainingMs = sleepMs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NowPlayingUiState()
    )
    
    fun togglePlayPause() {
        val player = playerManager.playerState.value
        if (player.isPlaying) {
            playerManager.pause()
        } else {
            playerManager.resume()
        }
    }

    fun next() = playerManager.next()
    fun previous() = playerManager.previous()
    fun seekInQueue(index: Int) = playerManager.seekInQueue(index)

    fun toggleFavorite() {
        val station = uiState.value.currentStation ?: return
        viewModelScope.launch { toggleFavoriteUseCase(station) }
    }

    // --- Add to playlist ---

    fun addStationToPlaylist(playlistId: Long) {
        val station = uiState.value.currentStation ?: return
        viewModelScope.launch { playlistRepository.addStationToPlaylist(playlistId, station) }
    }

    fun createPlaylistAndAddStation(name: String) {
        val station = uiState.value.currentStation ?: return
        viewModelScope.launch {
            val id = playlistRepository.createPlaylist(name)
            playlistRepository.addStationToPlaylist(id, station)
        }
    }

    fun startSleepTimer(durationMs: Long) = playerManager.startSleepTimer(durationMs)
    fun cancelSleepTimer() = playerManager.cancelSleepTimer()

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
    }
}
