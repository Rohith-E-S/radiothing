package com.radiothing.domain.model

data class AppSettings(
    val crossfadeDuration: Int = 3,
    val sleepTimerDuration: Int = 15,
    val useAsciiNotification: Boolean = false,
    val bufferSize: Int = 5000
)
