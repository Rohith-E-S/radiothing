package com.radiothing.player

import android.app.Application
import android.content.Intent
import androidx.media3.common.Player
import com.radiothing.domain.model.PlayerState
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.RecentlyPlayedRepository
import com.radiothing.domain.repository.SettingsRepository
import com.radiothing.player.service.RadioPlaybackService
import com.radiothing.player.timer.SleepTimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerManagerImpl @Inject constructor(
    private val application: Application,
    private val sleepTimerManager: SleepTimerManager,
    private val settingsRepository: SettingsRepository,
    private val recentlyPlayedRepository: RecentlyPlayedRepository
) : PlayerManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // --- State ---
    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume.asStateFlow()

    override val sleepTimerRemaining: StateFlow<Long> = sleepTimerManager.remainingMs

    // --- Command channels (consumed by RadioPlaybackService) ---
    private val _playCommand = MutableStateFlow<PlayCommand?>(null)
    override val playCommand: StateFlow<PlayCommand?> = _playCommand.asStateFlow()

    private val _pauseCommand = MutableStateFlow(false)
    override val pauseCommand: StateFlow<Boolean> = _pauseCommand.asStateFlow()

    private val _resumeCommand = MutableStateFlow(false)
    override val resumeCommand: StateFlow<Boolean> = _resumeCommand.asStateFlow()

    private val _volumeCommand = MutableStateFlow(-1f)
    override val volumeCommand: StateFlow<Float> = _volumeCommand.asStateFlow()

    // --- Internal ---
    private var crossfadeDurationMs = 0L

    init {
        scope.launch {
            settingsRepository.getSettings().collect { settings ->
                crossfadeDurationMs = settings.crossfadeDuration * 1000L
            }
        }
    }

    // --- UI → Service: send commands ---

    override fun play(station: RadioStation, queue: List<RadioStation>, queueIndex: Int) {
        _playerState.update {
            it.copy(
                currentStation = station,
                queue = queue,
                queueIndex = queueIndex,
                error = null,
                isBuffering = true
            )
        }
        // Start the service (foreground) before sending command
        ensureServiceRunning()
        _playCommand.value = PlayCommand(station, queue, queueIndex)
    }

    override fun pause() {
        _pauseCommand.value = true
    }

    override fun resume() {
        ensureServiceRunning()
        _resumeCommand.value = true
    }

    override fun stop() {
        _pauseCommand.value = true
        _playerState.update { it.copy(isPlaying = false) }
    }

    override fun next() {
        val state = _playerState.value
        if (state.queue.isNotEmpty()) {
            val next = (state.queueIndex + 1) % state.queue.size
            play(state.queue[next], state.queue, next)
        }
    }

    override fun previous() {
        val state = _playerState.value
        if (state.queue.isNotEmpty()) {
            val prev = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
            play(state.queue[prev], state.queue, prev)
        }
    }

    override fun setVolume(volume: Float) {
        _volume.value = volume
        _volumeCommand.value = volume
    }

    override fun seekInQueue(index: Int) {
        val state = _playerState.value
        if (index in state.queue.indices) {
            play(state.queue[index], state.queue, index)
        }
    }

    override fun setCrossfadeDuration(seconds: Int) {
        crossfadeDurationMs = seconds * 1000L
    }

    override fun startSleepTimer(durationMs: Long) {
        sleepTimerManager.start(durationMs, ::setVolume) { stop() }
    }

    override fun cancelSleepTimer() {
        sleepTimerManager.cancel()
    }

    override fun release() {
        sleepTimerManager.cancel()
    }

    // --- Command consumption (called by service after processing) ---

    override fun consumePlayCommand() { _playCommand.value = null }
    override fun consumePauseCommand() { _pauseCommand.value = false }
    override fun consumeResumeCommand() { _resumeCommand.value = false }
    override fun consumeVolumeCommand() { _volumeCommand.value = -1f }

    // --- Service → Manager: push state back to UI ---

    override fun onServicePlayingChanged(isPlaying: Boolean, player: Player?) {
        _playerState.update { it.copy(isPlaying = isPlaying) }
    }

    override fun onServiceBufferingChanged(isBuffering: Boolean) {
        _playerState.update { it.copy(isBuffering = isBuffering) }
    }

    override fun onServiceError(message: String?) {
        _playerState.update { it.copy(error = message, isPlaying = false, isBuffering = false) }
    }

    override fun attachServicePlayer(player: Player) {
        // Service is now active — state will come via callbacks
    }

    override fun detachServicePlayer() {
        _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
    }

    private fun ensureServiceRunning() {
        try {
            val intent = Intent(application, RadioPlaybackService::class.java)
            application.startForegroundService(intent)
        } catch (_: Exception) {}
    }
}
