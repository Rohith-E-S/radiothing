package com.radiothing.player

import androidx.media3.common.Player
import com.radiothing.domain.model.PlayerState
import com.radiothing.domain.model.RadioStation
import kotlinx.coroutines.flow.StateFlow

data class PlayCommand(val station: RadioStation, val queue: List<RadioStation>, val queueIndex: Int)

interface PlayerManager {
    val playerState: StateFlow<PlayerState>
    val volume: StateFlow<Float>
    val sleepTimerRemaining: StateFlow<Long>

    // Command channels (consumed by the service)
    val playCommand: StateFlow<PlayCommand?>
    val pauseCommand: StateFlow<Boolean>
    val resumeCommand: StateFlow<Boolean>
    val volumeCommand: StateFlow<Float> // -1f = no pending command

    // Called by UI / ViewModel
    fun play(station: RadioStation, queue: List<RadioStation> = emptyList(), queueIndex: Int = 0)
    fun pause()
    fun resume()
    fun stop()
    fun next()
    fun previous()
    fun setVolume(volume: Float)
    fun seekInQueue(index: Int)
    fun setCrossfadeDuration(seconds: Int)
    fun startSleepTimer(durationMs: Long)
    fun cancelSleepTimer()
    fun release()

    // Called by service to ack commands
    fun consumePlayCommand()
    fun consumePauseCommand()
    fun consumeResumeCommand()
    fun consumeVolumeCommand()

    // Called by service to push state back up
    fun onServicePlayingChanged(isPlaying: Boolean, player: Player?)
    fun onServiceBufferingChanged(isBuffering: Boolean)
    fun onServiceError(message: String?)
    fun attachServicePlayer(player: Player)
    fun detachServicePlayer()
}
