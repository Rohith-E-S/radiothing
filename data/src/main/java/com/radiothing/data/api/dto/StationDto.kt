package com.radiothing.data.api.dto

import com.google.gson.annotations.SerializedName
import com.radiothing.domain.model.RadioStation

data class StationDto(
    @SerializedName("stationuuid") val stationuuid: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("url_resolved") val urlResolved: String?,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("favicon") val favicon: String?,
    @SerializedName("tags") val tags: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("countrycode") val countrycode: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("codec") val codec: String?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("votes") val votes: Int?,
    @SerializedName("clickcount") val clickcount: Int?,
    @SerializedName("clicktrend") val clicktrend: Int?,
    @SerializedName("lastcheckok") val lastcheckok: Int?
) {
    fun toRadioStation(isFavorite: Boolean = false): RadioStation {
        return RadioStation(
            stationUuid = stationuuid.orEmpty(),
            name = name.orEmpty(),
            url = url.orEmpty(),
            urlResolved = urlResolved.orEmpty(),
            homepage = homepage.orEmpty(),
            favicon = favicon.orEmpty(),
            tags = tags.orEmpty(),
            country = country.orEmpty(),
            countryCode = countrycode.orEmpty(),
            language = language.orEmpty(),
            codec = codec.orEmpty(),
            bitrate = bitrate ?: 0,
            votes = votes ?: 0,
            clickCount = clickcount ?: 0,
            clickTrend = clicktrend ?: 0,
            lastCheckOk = lastcheckok == 1,
            isFavorite = isFavorite
        )
    }
}
