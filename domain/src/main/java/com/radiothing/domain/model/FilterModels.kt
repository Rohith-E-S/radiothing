package com.radiothing.domain.model

data class Country(
    val name: String,
    val stationCount: Int
)

data class Genre(
    val name: String,
    val stationCount: Int
)

data class Language(
    val name: String,
    val stationCount: Int
)

enum class StationOrder(val apiValue: String, val label: String) {
    VOTES("votes", "TOP VOTES"),
    NAME("name", "NAME"),
    BITRATE("bitrate", "BITRATE"),
    CLICKS("clicktrend", "TRENDING")
}

/** Server-side search/filter parameters for the community catalog. */
data class StationQuery(
    val name: String? = null,
    val tag: String? = null,
    val country: String? = null,
    val language: String? = null,
    val order: StationOrder = StationOrder.VOTES,
    val offset: Int = 0,
    val limit: Int = 20
)
