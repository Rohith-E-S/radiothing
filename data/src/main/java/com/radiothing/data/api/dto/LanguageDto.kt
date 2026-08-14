package com.radiothing.data.api.dto

import com.google.gson.annotations.SerializedName
import com.radiothing.domain.model.Language

data class LanguageDto(
    @SerializedName("name") val name: String?,
    @SerializedName("stationcount") val stationcount: Int?
) {
    fun toDomain(): Language {
        return Language(
            name = name.orEmpty(),
            stationCount = stationcount ?: 0
        )
    }
}
