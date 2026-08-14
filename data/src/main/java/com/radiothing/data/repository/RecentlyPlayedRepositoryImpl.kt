package com.radiothing.data.repository

import com.radiothing.data.db.dao.FavoriteDao
import com.radiothing.data.db.dao.RecentlyPlayedDao
import com.radiothing.data.db.entity.RecentlyPlayedEntity
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.RecentlyPlayedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class RecentlyPlayedRepositoryImpl @Inject constructor(
    private val dao: RecentlyPlayedDao,
    private val favoriteDao: FavoriteDao
) : RecentlyPlayedRepository {

    override fun getRecentlyPlayed(): Flow<List<RadioStation>> {
        return combine(dao.getAll(), favoriteDao.getAllIds()) { entities, favoriteIds ->
            entities.map { it.toRadioStation(isFavorite = favoriteIds.contains(it.stationUuid)) }
        }
    }

    override suspend fun addRecentlyPlayed(station: RadioStation) {
        dao.insert(
            RecentlyPlayedEntity(
                stationUuid = station.stationUuid,
                name = station.name,
                url = station.url,
                urlResolved = station.urlResolved,
                homepage = station.homepage,
                favicon = station.favicon,
                tags = station.tags,
                country = station.country,
                countryCode = station.countryCode,
                language = station.language,
                codec = station.codec,
                bitrate = station.bitrate,
                votes = station.votes,
                clickCount = station.clickCount,
                clickTrend = station.clickTrend,
                lastCheckOk = station.lastCheckOk,
                playedAt = System.currentTimeMillis()
            )
        )
        dao.deleteOldest()
    }

    override suspend fun clearHistory() {
        dao.clear()
    }
}
