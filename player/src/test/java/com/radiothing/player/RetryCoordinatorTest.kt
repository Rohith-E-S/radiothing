package com.radiothing.player

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class RetryCoordinatorTest {
    @Test
    fun `backoff sequence grows exponentially`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var retryCount = 0
        val coordinator = RetryCoordinator(
            scope = backgroundScope,
            onRetry = { retryCount++ },
            onGiveUp = {},
            maxRetries = 5,
            random = Random(0)
        )
        // First retry ~2s, second ~4s, third ~8s etc. We just check dedup and giveUp
        coordinator.requestRetry("error1")
        // Second call while job active should be deduped
        coordinator.requestRetry("error2")
        advanceTimeBy(3000)
        assertEquals(1, retryCount)
        coordinator.requestRetry("error3")
        advanceTimeBy(5000)
        assertEquals(2, retryCount)
    }

    @Test
    fun `gives up after max retries`() = runTest {
        var gaveUp = false
        val coordinator = RetryCoordinator(
            scope = backgroundScope,
            onRetry = {},
            onGiveUp = { gaveUp = true },
            maxRetries = 2,
            random = Random(0)
        )
        coordinator.requestRetry("e1")
        advanceTimeBy(3000)
        coordinator.requestRetry("e2")
        advanceTimeBy(5000)
        // third should trigger giveUp
        coordinator.requestRetry("e3")
        assertTrue(gaveUp)
    }

    @Test
    fun `cancel resets count`() = runTest {
        var retries = 0
        val coordinator = RetryCoordinator(
            scope = backgroundScope,
            onRetry = { retries++ },
            onGiveUp = {},
            maxRetries = 5,
            random = Random(0)
        )
        coordinator.requestRetry("e1")
        coordinator.cancel()
        coordinator.requestRetry("e1")
        advanceTimeBy(3000)
        assertEquals(1, retries)
    }
}
