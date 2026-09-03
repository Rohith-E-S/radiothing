package com.radiothing.player.service

import androidx.media3.exoplayer.DefaultLoadControl

object LoadControlFactory {
    const val MIN_BUFFER_MS = 2_000
    const val MAX_BUFFER_MS = 30_000
    const val BUFFER_FOR_PLAYBACK_MS = 1_000
    const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2_000

    fun from(bufferMs: Int): DefaultLoadControl {
        val min = bufferMs.coerceIn(MIN_BUFFER_MS, MAX_BUFFER_MS)
        val max = (bufferMs * 3).coerceIn(MIN_BUFFER_MS, MAX_BUFFER_MS)
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(min, max, BUFFER_FOR_PLAYBACK_MS, BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }
}
