package com.radiothing.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.radiothing.domain.model.RadioStation

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey
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
    val playedAt: Long
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
