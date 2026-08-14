package com.radiothing.domain.usecase

import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.PlaylistRepository
import javax.inject.Inject

class ManagePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return playlistRepository.createPlaylist(name, description)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
    }

    suspend fun addStationToPlaylist(playlistId: Long, station: RadioStation) {
        playlistRepository.addStationToPlaylist(playlistId, station)
    }

    suspend fun removeStationFromPlaylist(playlistId: Long, stationUuid: String) {
        playlistRepository.removeStationFromPlaylist(playlistId, stationUuid)
    }

    suspend fun reorderStations(playlistId: Long, stationUuids: List<String>) {
        playlistRepository.reorderStations(playlistId, stationUuids)
    }
}
