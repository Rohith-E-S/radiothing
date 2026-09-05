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

    private val _audioSessionId = MutableStateFlow(0)
    override val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()
    override fun onServiceAudioSessionIdChanged(sessionId: Int) {
        _audioSessionId.value = sessionId
    }

    // --- Internal ---
    private var crossfadeDurationMs = 0L

    /** Whether the service's ExoPlayer is attached and has something prepared. */
    private var serviceAttached = false
    private var playedSinceAttach = false

    init {
        scope.launch {
            settingsRepository.getSettings().collect { settings ->
                crossfadeDurationMs = settings.crossfadeDuration * 1000L
            }
        }
        restoreLastSession()
    }

    override fun restoreLastSession() {
        scope.launch {
            try {
                val history = recentlyPlayedRepository.getRecentlyPlayedOnce()
                if (history.isNotEmpty() && _playerState.value.currentStation == null) {
                    val station = history.first()
                    _playerState.update {
                        it.copy(
                            currentStation = station,
                            queue = history,
                            queueIndex = 0,
                            isPlaying = false,
                            isBuffering = false
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // --- UI → Service: send commands ---

    override fun play(station: RadioStation, queue: List<RadioStation>, queueIndex: Int) {
        playedSinceAttach = true
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
        if (serviceAttached && playedSinceAttach) {
            _resumeCommand.value = true
        } else {
            // Service is fresh (never attached this session, or restarted with
            // nothing prepared) — resume would be a no-op on an empty player.
            // Replay the current station instead so the visible play button works.
            val state = _playerState.value
            state.currentStation?.let { play(it, state.queue, state.queueIndex) }
        }
    }

    override fun stop() {
        _pauseCommand.value = true
        _playerState.update { it.copy(isPlaying = false) }
        // Sleep-timer fade ended playback — restore pre-fade volume so the
        // next play isn't silent.
        sleepTimerManager.consumePreFadeVolume()?.let { restored ->
            _volume.value = restored
            _volumeCommand.value = restored
        }
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
        // Snapshot current volume so the fade can be undone later
        sleepTimerManager.currentVolumeSnapshot = _volume.value
        sleepTimerManager.start(durationMs, ::fadeVolume) { stop() }
    }

    override fun cancelSleepTimer() {
        sleepTimerManager.cancel()
        // If a fade was mid-flight, bring volume back immediately
        sleepTimerManager.consumePreFadeVolume()?.let { restored ->
            _volume.value = restored
            _volumeCommand.value = restored
        }
    }

    /** Fade channel — lowers audible volume without destroying the user's set volume. */
    private fun fadeVolume(v: Float) {
        _volumeCommand.value = v.coerceIn(0f, _volume.value)
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
        if (isPlaying) playedSinceAttach = true
        _playerState.update { it.copy(isPlaying = isPlaying) }
    }

    override fun onServiceBufferingChanged(isBuffering: Boolean) {
        _playerState.update { it.copy(isBuffering = isBuffering) }
    }

    override fun onServiceError(message: String?) {
        _playerState.update { it.copy(error = message, isPlaying = false, isBuffering = false) }
    }

    override fun attachServicePlayer(player: Player) {
        serviceAttached = true
        playedSinceAttach = false
        // State will come via callbacks
    }

    override fun detachServicePlayer() {
        serviceAttached = false
        playedSinceAttach = false
        _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
    }

    private fun ensureServiceRunning() {
        try {
            val intent = Intent(application, RadioPlaybackService::class.java)
            application.startForegroundService(intent)
        } catch (e: IllegalStateException) {
            // Android 12+: startForegroundService from the background throws
            // ForegroundServiceStartNotAllowedException (an IllegalStateException).
            // Previously swallowed — the command was published with no service to
            // consume it, leaving the UI buffering forever with no error.
            _playerState.update {
                it.copy(error = "PLAYBACK BLOCKED — OPEN APP TO START", isBuffering = false, isPlaying = false)
            }
        } catch (_: Exception) {}
    }
}
