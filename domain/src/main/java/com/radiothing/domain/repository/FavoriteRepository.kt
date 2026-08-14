package com.radiothing.domain.repository

import com.radiothing.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(): Flow<List<RadioStation>>
    suspend fun addFavorite(station: RadioStation)
    suspend fun removeFavorite(stationUuid: String)
    suspend fun isFavorite(stationUuid: String): Boolean
    fun getFavoriteIds(): Flow<Set<String>>
}
