package com.radiothing.data.db.entity

import androidx.room.Entity
import com.radiothing.domain.model.RadioStation

@Entity(
    tableName = "playlist_stations",
    primaryKeys = ["playlistId", "stationUuid"]
)
data class PlaylistStationEntity(
    val playlistId: Long,
    val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String,
    val favicon: String,
    val tags: String,
    val country: String,
    val countryCode: String,
    val language: String,
    val codec: String,
    val bitrate: Int,
    val votes: Int,
    val clickCount: Int,
    val clickTrend: Int,
    val lastCheckOk: Boolean,
    val orderIndex: Int
) {
    fun toRadioStation(isFavorite: Boolean = false): RadioStation {
        return RadioStation(
            stationUuid = stationUuid,
            name = name,
            url = url,
            urlResolved = urlResolved,
            homepage = homepage,
            favicon = favicon,
            tags = tags,
            country = country,
            countryCode = countryCode,
            language = language,
            codec = codec,
            bitrate = bitrate,
            votes = votes,
            clickCount = clickCount,
            clickTrend = clickTrend,
            lastCheckOk = lastCheckOk,
            isFavorite = isFavorite
        )
    }
}
