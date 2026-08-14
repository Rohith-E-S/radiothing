package com.radiothing.domain.model

data class RadioStation(
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
    val isFavorite: Boolean = false
)
