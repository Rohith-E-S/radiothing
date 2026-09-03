package com.radiothing.domain.repository

import com.radiothing.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

interface RecentlyPlayedRepository {
    fun getRecentlyPlayed(): Flow<List<RadioStation>>
    suspend fun getRecentlyPlayedOnce(limit: Int = 20): List<RadioStation>
    suspend fun addRecentlyPlayed(station: RadioStation)
    suspend fun clearHistory()
}
