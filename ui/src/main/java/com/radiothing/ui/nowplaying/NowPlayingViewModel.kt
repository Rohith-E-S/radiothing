package com.radiothing.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.PlayerState
import com.radiothing.domain.model.RadioStation
import com.radiothing.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NowPlayingUiState(
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val volume: Float = 0.5f
)

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    
    val uiState: StateFlow<NowPlayingUiState> = combine(
        _uiState,
        playerManager.playerState,
        playerManager.volume
    ) { state, player, volume ->
        state.copy(
            currentStation = player.currentStation,
            isPlaying = player.isPlaying,
            isBuffering = player.isBuffering,
            volume = volume
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

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
    }
}
