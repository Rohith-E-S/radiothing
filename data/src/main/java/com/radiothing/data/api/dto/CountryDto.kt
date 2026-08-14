package com.radiothing.data.api.dto

import com.google.gson.annotations.SerializedName
import com.radiothing.domain.model.Country

data class CountryDto(
    @SerializedName("name") val name: String?,
    @SerializedName("stationcount") val stationcount: Int?
) {
    fun toDomain(): Country {
        return Country(
            name = name.orEmpty(),
            stationCount = stationcount ?: 0
        )
    }
}
