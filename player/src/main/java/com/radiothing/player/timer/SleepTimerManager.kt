package com.radiothing.player.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

class SleepTimerManager @Inject constructor() {
    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Volume the user had before the fade began — restored when playback stops
    // so the next play isn't silent.
    private var preFadeVolume: Float? = null

    fun start(durationMs: Long, setVolume: (Float) -> Unit, stopPlayback: () -> Unit) {
        cancel()
        _remainingMs.value = durationMs

        job = scope.launch {
            val fadeOutDurationMs = 30_000L
            var fadeStarted = false
            while (_remainingMs.value > 0) {
                delay(1000)
                _remainingMs.value -= 1000

                if (_remainingMs.value <= fadeOutDurationMs && _remainingMs.value > 0) {
                    if (!fadeStarted) {
                        fadeStarted = true
                        // Snapshot only once, before the first fade step
                        if (preFadeVolume == null) preFadeVolume = currentVolumeSnapshot
                    }
                    val volume = _remainingMs.value.toFloat() / fadeOutDurationMs
                    setVolume(max(0f, volume))
                }
            }
            _remainingMs.value = 0
            stopPlayback()
        }
    }

    /** Latest volume as observed through [setVolume]; set by the owner before fade. */
    var currentVolumeSnapshot: Float = 1f

    /** Restore the pre-fade volume (returns null if no fade ever happened). */
    fun consumePreFadeVolume(): Float? {
        val v = preFadeVolume
        preFadeVolume = null
        return v
    }

    fun cancel() {
        // A cancel mid-fade must also restore, else user stays muted
        job?.cancel()
        job = null
        _remainingMs.value = 0
    }
}
