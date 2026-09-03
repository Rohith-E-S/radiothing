package com.radiothing.player

/**
 * Maps raw stream/network failures to the instrument's honest voice.
 * Red means live — errors must read like the bench, not a stack trace.
 */
object StreamErrorMessages {

    fun from(throwable: Throwable?): String = when (throwable) {
        null -> "SIGNAL LOST — RETRY"
        is java.net.UnknownHostException -> "NO SIGNAL — CHECK CONNECTION"
        is java.net.SocketTimeoutException -> "SIGNAL TIMEOUT — RETRY"
        is java.net.ConnectException -> "STATION OFFLINE — TRY NEXT"
        is java.io.IOException -> "SIGNAL LOST — CHECK CONNECTION"
        else -> fromMessage(throwable.message)
    }

    fun fromMessage(message: String?): String {
        val m = message?.lowercase() ?: return "SIGNAL LOST — RETRY"
        return when {
            "unable to resolve" in m || "unknownhost" in m -> "NO SIGNAL — CHECK CONNECTION"
            "timeout" in m || "timed out" in m -> "SIGNAL TIMEOUT — RETRY"
            "connection" in m && ("refused" in m || "reset" in m) -> "STATION OFFLINE — TRY NEXT"
            "404" in m || "not found" in m -> "STATION GONE — TRY NEXT"
            "403" in m || "forbidden" in m -> "STATION BLOCKED — TRY NEXT"
            "http" in m && "500" in m -> "STATION FAULT — TRY NEXT"
            "source error" in m -> "SIGNAL LOST — RETRY"
            else -> "SIGNAL LOST — RETRY"
        }
    }
}
