package com.radiothing.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.radiothing.domain.model.RadioStation

@Entity(tableName = "favorites")
data class FavoriteEntity(
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
    val lastCheckOk: Boolean
) {
    fun toRadioStation(): RadioStation {
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
            isFavorite = true
        )
    }

    companion object {
        fun fromRadioStation(station: RadioStation): FavoriteEntity {
            return FavoriteEntity(
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
                lastCheckOk = station.lastCheckOk
            )
        }
    }
}
