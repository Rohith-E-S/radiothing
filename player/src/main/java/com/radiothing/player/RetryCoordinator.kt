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
 * Calling [cancel] (e.g. when user manually tunes a new station) clears pending
 * retries and resets the failure count; [cancelPendingRetry] only drops a
 * scheduled retry and preserves the count so give-up stays reachable.
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
                // Clear before invoking onRetry: the retry re-enters playback setup
                // which may call cancelPendingRetry — cancelling our own running
                // job from inside it would be murky. After this, the job is done.
                retryJob = null
                onRetry()
            } catch (_: Exception) {
                // Swallow cancellations / unexpected errors — watchdog will retry again if still stuck
            }
        }
    }

    /**
     * Full reset: cancels any pending retry and clears the failure count.
     * Only for user-initiated tuning or teardown — resetting the count on a
     * retry re-entry would make maxRetries unreachable.
     */
    fun cancel() {
        retryJob?.cancel()
        retryJob = null
        retryCount = 0
    }

    /**
     * Cancels a scheduled retry without resetting the failure count. Used when
     * a retry re-enters playback setup so consecutive failures keep counting
     * toward [maxRetries].
     */
    fun cancelPendingRetry() {
        retryJob?.cancel()
        retryJob = null
    }

    fun resetCount() {
        retryCount = 0
    }

    fun isRetrying(): Boolean = retryJob?.isActive == true
}
