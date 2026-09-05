package com.radiothing.player

import androidx.media3.common.PlaybackException

/**
 * Maps raw stream/network failures to the instrument's honest voice.
 * Red means live — errors must read like the bench, not a stack trace.
 */
object StreamErrorMessages {

    fun from(throwable: Throwable?): String {
        if (throwable == null) return "SIGNAL LOST — RETRY"
        // ExoPlayer wraps nearly every failure in PlaybackException with a
        // stable error code — branch on that before message sniffing
        if (throwable is PlaybackException) return fromErrorCode(throwable.errorCode, throwable.message)
        // The code may also be buried in the cause chain
        var cause = throwable.cause
        while (cause != null) {
            if (cause is PlaybackException) return fromErrorCode(cause.errorCode, cause.message)
            cause = cause.cause
        }
        return when (throwable) {
            is java.net.UnknownHostException -> "NO SIGNAL — CHECK CONNECTION"
            is java.net.SocketTimeoutException -> "SIGNAL TIMEOUT — RETRY"
            is java.net.ConnectException -> "STATION OFFLINE — TRY NEXT"
            is java.io.IOException -> "SIGNAL LOST — CHECK CONNECTION"
            else -> fromMessage(throwable.message)
        }
    }

    private fun fromErrorCode(errorCode: Int, message: String?): String = when (errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "NO SIGNAL — CHECK CONNECTION"
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "SIGNAL TIMEOUT — RETRY"
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
        PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED -> "STATION BLOCKED — TRY NEXT"
        PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE -> "SIGNAL LOST — RETRY"
        PlaybackException.ERROR_CODE_TIMEOUT -> "SIGNAL TIMEOUT — RETRY"
        PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> "SIGNAL LOST — RETRY"
        else -> fromMessage(message)
    }

    fun fromMessage(message: String?): String {
        val m = message?.lowercase() ?: return "SIGNAL LOST — RETRY"
        return when {
            "unable to resolve" in m || "unknownhost" in m -> "NO SIGNAL — CHECK CONNECTION"
            "timeout" in m || "timed out" in m -> "SIGNAL TIMEOUT — RETRY"
            "connection" in m && ("refused" in m || "reset" in m) -> "STATION OFFLINE — TRY NEXT"
            "404" in m || "not found" in m -> "STATION GONE — TRY NEXT"
            "403" in m || "forbidden" in m -> "STATION BLOCKED — TRY NEXT"
            // ExoPlayer surfaces HTTP failures as "Response code: 500" — no "http" substring
            "response code: 5" in m || ("http" in m && "500" in m) -> "STATION FAULT — TRY NEXT"
            "source error" in m -> "SIGNAL LOST — RETRY"
            else -> "SIGNAL LOST — RETRY"
        }
    }
}
