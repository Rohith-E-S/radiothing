package com.radiothing.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PlaylistWithStations(
    val playlist: Playlist,
    val stations: List<RadioStation>
)
