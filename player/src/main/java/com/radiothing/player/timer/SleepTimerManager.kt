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

    fun start(durationMs: Long, setVolume: (Float) -> Unit, stopPlayback: () -> Unit) {
        cancel()
        _remainingMs.value = durationMs

        job = scope.launch {
            val fadeOutDurationMs = 30_000L
            while (_remainingMs.value > 0) {
                delay(1000)
                _remainingMs.value -= 1000

                if (_remainingMs.value <= fadeOutDurationMs && _remainingMs.value > 0) {
                    val volume = _remainingMs.value.toFloat() / fadeOutDurationMs
                    setVolume(max(0f, volume))
                }
            }
            _remainingMs.value = 0
            stopPlayback()
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        _remainingMs.value = 0
    }
}
