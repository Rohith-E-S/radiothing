package com.radiothing.player.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerManagerTest {

    private val volumes = mutableListOf<Float>()
    private var stopped = false
    private var userVolume = 0.8f

    private fun kotlinx.coroutines.test.TestScope.runTestWithTimer(): SleepTimerManager =
        SleepTimerManager().apply {
            scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        }.also {
            it.start(
                timerDurationMs,
                setVolume = { volumes.add(it) },
                stopPlayback = { stopped = true },
                volumeSnapshot = { userVolume }
            )
        }

    private var timerDurationMs = 40_000L

    private fun kotlinx.coroutines.test.TestScope.tick(ms: Long) = advanceTimeBy(ms)

    @Test
    fun `short timer fades proportionally instead of dropping instantly`() = runTest {
        timerDurationMs = 10_000L
        val manager = runTestWithTimer()

        // 2.5 ticks → remaining 8s of a 10s timer → 0.8, not 8/30 ≈ 0.27 (fixed 30s window bug)
        tick(2_500)
        assertEquals(listOf(0.9f, 0.8f), volumes)
        assertEquals(8_000L, manager.remainingMs.value)
    }

    @Test
    fun `re-arming mid-fade restores pre-fade volume before restarting`() = runTest {
        timerDurationMs = 40_000L
        val manager = runTestWithTimer()

        // 11.5 ticks → fade started at remaining=30s (volume 1.0), now 29s → ~0.967
        tick(11_500)
        assertEquals(29_000L, manager.remainingMs.value)
        assertTrue(volumes.last() < 1.0f)

        // Re-arm a fresh long timer — the mid-fade volume must be undone first
        userVolume = 0.8f
        manager.start(
            60_000,
            setVolume = { volumes.add(it) },
            stopPlayback = { stopped = true },
            volumeSnapshot = { userVolume }
        )
        assertEquals(0.8f, volumes.last(), 0.0001f)
    }

    @Test
    fun `snapshot is read at fade start, not when timer was armed`() = runTest {
        timerDurationMs = 40_000L
        val manager = runTestWithTimer()

        // User changes volume well before the fade window begins
        userVolume = 0.5f
        tick(11_000)

        assertEquals(0.5f, manager.consumePreFadeVolume()!!, 0.0001f)
    }

    @Test
    fun `timer completes, stops playback and leaves pre-fade volume for restore`() = runTest {
        timerDurationMs = 2_000L
        val manager = runTestWithTimer()

        tick(3_000)

        assertTrue(stopped)
        assertEquals(0L, manager.remainingMs.value)
        // The owner's stop path consumes and restores it; verify it's available
        assertEquals(0.8f, manager.consumePreFadeVolume()!!, 0.0001f)
        assertNull(manager.consumePreFadeVolume())
    }

    @Test
    fun `cancel mid-fade stops the countdown and keeps pre-fade volume`() = runTest {
        timerDurationMs = 40_000L
        val manager = runTestWithTimer()

        tick(35_000)
        manager.cancel()

        assertFalse(manager.remainingMs.value > 0)
        assertEquals(0.8f, manager.consumePreFadeVolume()!!, 0.0001f)
    }
}
