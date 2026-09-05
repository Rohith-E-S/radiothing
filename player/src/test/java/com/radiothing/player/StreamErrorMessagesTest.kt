package com.radiothing.player

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class StreamErrorMessagesTest {

    @Test
    fun `playback exception error code drives the message`() {
        val e = PlaybackException("connect failed", null, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        assertEquals("NO SIGNAL — CHECK CONNECTION", StreamErrorMessages.from(e))
    }

    @Test
    fun `network timeout error code maps to timeout message`() {
        val e = PlaybackException(null, null, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
        assertEquals("SIGNAL TIMEOUT — RETRY", StreamErrorMessages.from(e))
    }

    @Test
    fun `cleartext traffic maps to station blocked`() {
        val e = PlaybackException(null, null, PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED)
        assertEquals("STATION BLOCKED — TRY NEXT", StreamErrorMessages.from(e))
    }

    @Test
    fun `error code buried in cause chain is unwrapped`() {
        val inner = PlaybackException(null, null, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        val outer = RuntimeException("Source error", inner)
        assertEquals("NO SIGNAL — CHECK CONNECTION", StreamErrorMessages.from(outer))
    }

    @Test
    fun `response code 500 without http substring maps to station fault`() {
        assertEquals("STATION FAULT — TRY NEXT", StreamErrorMessages.fromMessage("Response code: 500"))
    }

    @Test
    fun `unknown host and socket timeout map directly`() {
        assertEquals("NO SIGNAL — CHECK CONNECTION", StreamErrorMessages.from(UnknownHostException("x")))
        assertEquals("SIGNAL TIMEOUT — RETRY", StreamErrorMessages.from(SocketTimeoutException("x")))
    }

    @Test
    fun `unknown codes and nulls fall back to generic`() {
        assertEquals("SIGNAL LOST — RETRY", StreamErrorMessages.from(null))
        assertEquals("SIGNAL LOST — RETRY", StreamErrorMessages.fromMessage(null))
        val e = PlaybackException(null, null, PlaybackException.ERROR_CODE_UNSPECIFIED)
        assertEquals("SIGNAL LOST — RETRY", StreamErrorMessages.from(e))
    }
}
