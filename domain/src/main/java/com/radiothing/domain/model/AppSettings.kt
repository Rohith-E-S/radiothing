package com.radiothing.domain.model

data class AppSettings(
    val useAsciiNotification: Boolean = false,
    val bufferSize: Int = 5000,
)
