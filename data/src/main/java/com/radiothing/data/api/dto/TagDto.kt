package com.radiothing.data.api.dto

import com.google.gson.annotations.SerializedName
import com.radiothing.domain.model.Genre

data class TagDto(
    @SerializedName("name") val name: String?,
    @SerializedName("stationcount") val stationcount: Int?
) {
    fun toDomain(): Genre {
        return Genre(
            name = name.orEmpty(),
            stationCount = stationcount ?: 0
        )
    }
}
