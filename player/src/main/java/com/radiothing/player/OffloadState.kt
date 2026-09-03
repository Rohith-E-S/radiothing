package com.radiothing.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether audio offload is currently active. When true, the Visualizer
 * cannot capture audio and the UI should use the synthetic fallback.
 */
@Singleton
class OffloadState @Inject constructor() {
    private val _isOffloaded = MutableStateFlow(false)
    val isOffloaded: StateFlow<Boolean> = _isOffloaded.asStateFlow()

    fun setOffloaded(offloaded: Boolean) {
        _isOffloaded.value = offloaded
    }
}
