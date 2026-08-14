package com.radiothing.domain.model

data class PlayerState(
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null,
    val queue: List<RadioStation> = emptyList(),
    val queueIndex: Int = -1
)
