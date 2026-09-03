package com.radiothing.domain.repository

import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.PlaylistWithStations
import com.radiothing.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistWithStations(playlistId: Long): Flow<PlaylistWithStations?>
    fun getPlaylistStationCounts(): Flow<Map<Long, Int>>
    suspend fun createPlaylist(name: String, description: String = ""): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addStationToPlaylist(playlistId: Long, station: RadioStation)
    suspend fun removeStationFromPlaylist(playlistId: Long, stationUuid: String)
    suspend fun reorderStations(playlistId: Long, stationUuids: List<String>)
}
