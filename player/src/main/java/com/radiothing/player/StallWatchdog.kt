package com.radiothing.player

/**
 * Detects stalls in live radio playback by tracking whether the playback position
 * has advanced. A stall is declared when the position has not moved for
 * [stallThresholdMs] while the player reports isPlaying=true and isBuffering=false.
 *
 * Pure logic — no Android dependencies, fully unit-testable.
 */
class StallWatchdog(
    private val stallThresholdMs: Long = 15_000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private var lastPositionMs: Long = Long.MIN_VALUE
    private var lastProgressTimeMs: Long = 0L

    fun reset() {
        lastPositionMs = Long.MIN_VALUE
        lastProgressTimeMs = clock()
    }

    /**
     * Call periodically (e.g. every 5s). Returns true if a stall is detected.
     */
    fun tick(currentPositionMs: Long, isPlaying: Boolean, isBuffering: Boolean): Boolean {
        val now = clock()
        if (!isPlaying || isBuffering) {
            // Not in a state where stall matters — update baseline but never report stall
            lastPositionMs = currentPositionMs
            lastProgressTimeMs = now
            return false
        }
        if (currentPositionMs != lastPositionMs) {
            lastPositionMs = currentPositionMs
            lastProgressTimeMs = now
            return false
        }
        // Position hasn't moved
        return (now - lastProgressTimeMs) >= stallThresholdMs
    }
}
