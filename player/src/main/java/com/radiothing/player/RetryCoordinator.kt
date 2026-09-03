package com.radiothing.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Centralizes retry logic for stream errors and stall watchdog events.
 * Uses jittered exponential backoff capped at 30s, max 5 retries then gives up.
 *
 * Both [onPlaybackError] and [onStallDetected] funnel through [requestRetry].
 * Calling [cancel] (e.g. when user manually tunes a new station) clears pending retries.
 */
class RetryCoordinator(
    private val scope: CoroutineScope,
    private val onRetry: suspend () -> Unit,
    private val onGiveUp: (String) -> Unit,
    private val maxRetries: Int = 5,
    private val random: Random = Random.Default
) {
    private var retryCount = 0
    private var retryJob: Job? = null

    fun requestRetry(reason: String) {
        if (retryJob?.isActive == true) return // dedup: already retrying
        if (retryCount >= maxRetries) {
            try { onGiveUp(reason) } catch (_: Exception) {}
            return
        }
        retryCount++
        val baseDelay = minOf(30_000L, 2_000L * (1L shl (retryCount - 1)))
        val jitter = random.nextLong(0, 1_000)
        val delayMs = baseDelay + jitter
        retryJob = scope.launch {
            try {
                delay(delayMs)
                onRetry()
            } catch (_: Exception) {
                // Swallow cancellations / unexpected errors — watchdog will retry again if still stuck
            }
        }
    }

    fun cancel() {
        retryJob?.cancel()
        retryJob = null
        retryCount = 0
    }

    fun resetCount() {
        retryCount = 0
    }

    fun isRetrying(): Boolean = retryJob?.isActive == true
}
