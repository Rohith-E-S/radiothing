package com.radiothing.app

import androidx.lifecycle.ViewModel
import com.radiothing.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    _playerManager: PlayerManager
) : ViewModel() {

    val playerState = _playerManager.playerState
    val playerManager = _playerManager

    fun playPause() {
        val state = playerState.value
        if (state.isPlaying) {
            playerManager.pause()
        } else {
            playerManager.resume()
        }
    }

    fun next() = playerManager.next()

    fun previous() = playerManager.previous()
}
