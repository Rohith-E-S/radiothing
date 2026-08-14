package com.radiothing.data.repository

import com.radiothing.data.db.dao.FavoriteDao
import com.radiothing.data.db.entity.FavoriteEntity
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<RadioStation>> {
        return dao.getAll().map { entities ->
            entities.map { it.toRadioStation() }
        }
    }

    override suspend fun addFavorite(station: RadioStation) {
        dao.insert(FavoriteEntity.fromRadioStation(station))
    }

    override suspend fun removeFavorite(stationUuid: String) {
        dao.delete(stationUuid)
    }

    override suspend fun isFavorite(stationUuid: String): Boolean {
        return dao.exists(stationUuid)
    }

    override fun getFavoriteIds(): Flow<Set<String>> {
        return dao.getAllIds().map { it.toSet() }
    }
}
