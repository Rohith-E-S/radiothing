package com.radiothing.data.repository

import androidx.room.withTransaction
import com.radiothing.data.db.RadioDatabase
import com.radiothing.data.db.dao.FavoriteDao
import com.radiothing.data.db.dao.PlaylistDao
import com.radiothing.data.db.entity.PlaylistEntity
import com.radiothing.data.db.entity.PlaylistStationEntity
import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.PlaylistWithStations
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val dao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val database: RadioDatabase
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        return dao.getPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPlaylistStationCounts(): Flow<Map<Long, Int>> {
        return dao.getPlaylistStationCounts().map { counts ->
            counts.associate { it.playlistId to it.stationCount }
        }
    }

    override fun getPlaylistWithStations(playlistId: Long): Flow<PlaylistWithStations?> {
        val playlistFlow = dao.getPlaylistById(playlistId)
        val stationsFlow = dao.getPlaylistStations(playlistId)
        val favoritesFlow = favoriteDao.getAllIds()

        return combine(playlistFlow, stationsFlow, favoritesFlow) { playlist, stations, favoriteIds ->
            if (playlist == null) return@combine null
            
            PlaylistWithStations(
                playlist = playlist.toDomain(),
                stations = stations.map { it.toRadioStation(isFavorite = favoriteIds.contains(it.stationUuid)) }
            )
        }
    }

    override suspend fun createPlaylist(name: String, description: String): Long {
        return dao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        dao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                description = playlist.description,
                createdAt = playlist.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylistWithStations(playlistId)
    }

    override suspend fun addStationToPlaylist(playlistId: Long, station: RadioStation) {
        val currentCount = dao.getStationCount(playlistId)
        dao.insertPlaylistStation(
            PlaylistStationEntity(
                playlistId = playlistId,
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
                orderIndex = currentCount
            )
        )
    }

    override suspend fun removeStationFromPlaylist(playlistId: Long, stationUuid: String) {
        dao.removePlaylistStation(playlistId, stationUuid)
    }

    override suspend fun reorderStations(playlistId: Long, stationUuids: List<String>) {
        database.withTransaction {
            stationUuids.forEachIndexed { index, uuid ->
                dao.updateStationOrder(playlistId, uuid, index)
            }
        }
    }
}
