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

    /** Overridable for tests (production: Default dispatcher). */
    internal var scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    // Volume the user had before the fade began — restored when playback stops
    // or the timer is replaced, so the next play isn't silent.
    private var preFadeVolume: Float? = null

    fun start(
        durationMs: Long,
        setVolume: (Float) -> Unit,
        stopPlayback: () -> Unit,
        /** Live user volume, read when the fade actually begins. */
        volumeSnapshot: () -> Float
    ) {
        cancel()
        // Re-arming mid-fade must undo the old fade first, else the user sits
        // at near-zero volume until this timer's own fade window begins.
        consumePreFadeVolume()?.let(setVolume)

        _remainingMs.value = durationMs

        job = scope.launch {
            // Scale the fade window to the timer — a fixed 30s window made a
            // 10s timer drop the volume to ~0.3 on its first tick.
            val fadeOutDurationMs = minOf(30_000L, durationMs)
            var fadeStarted = false
            while (_remainingMs.value > 0) {
                delay(1000)
                _remainingMs.value -= 1000

                if (_remainingMs.value <= fadeOutDurationMs && _remainingMs.value > 0) {
                    if (!fadeStarted) {
                        fadeStarted = true
                        // Snapshot at fade start through the provider: fades go
                        // through the command channel, not the user's volume
                        // state, so this stays correct even mid-fade.
                        preFadeVolume = volumeSnapshot()
                    }
                    val volume = _remainingMs.value.toFloat() / fadeOutDurationMs
                    setVolume(max(0f, volume))
                }
            }
            _remainingMs.value = 0
            stopPlayback()
        }
    }

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
