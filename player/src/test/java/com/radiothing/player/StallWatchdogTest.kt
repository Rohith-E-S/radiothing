package com.radiothing.player

import org.junit.Assert.*
import org.junit.Test

class StallWatchdogTest {
    @Test
    fun `does not report stall when position advances`() {
        var now = 0L
        val w = StallWatchdog(stallThresholdMs = 10_000L, clock = { now })
        w.reset()
        assertFalse(w.tick(0L, true, false))
        now = 5_000L
        assertFalse(w.tick(1000L, true, false))
        now = 12_000L
        assertFalse(w.tick(2000L, true, false))
    }

    @Test
    fun `reports stall after threshold when position stuck`() {
        var now = 0L
        val w = StallWatchdog(stallThresholdMs = 15_000L, clock = { now })
        w.reset()
        assertFalse(w.tick(5000L, true, false))
        now = 10_000L
        assertFalse(w.tick(5000L, true, false))
        now = 20_000L
        assertTrue(w.tick(5000L, true, false))
    }

    @Test
    fun `does not stall when buffering`() {
        var now = 0L
        val w = StallWatchdog(clock = { now })
        w.reset()
        w.tick(100L, true, false)
        now = 20_000L
        // buffering=true should reset baseline, not stall
        assertFalse(w.tick(100L, true, true))
        now = 40_000L
        assertFalse(w.tick(100L, true, true))
    }

    @Test
    fun `does not stall when not playing`() {
        var now = 0L
        val w = StallWatchdog(clock = { now })
        w.reset()
        w.tick(100L, false, false)
        now = 30_000L
        assertFalse(w.tick(100L, false, false))
    }

    @Test
    fun `reset clears stall history`() {
        var now = 0L
        val w = StallWatchdog(stallThresholdMs = 5_000L, clock = { now })
        w.reset()
        w.tick(100L, true, false)
        now = 10_000L
        assertTrue(w.tick(100L, true, false))
        w.reset()
        now = 11_000L
        assertFalse(w.tick(100L, true, false))
    }
}
