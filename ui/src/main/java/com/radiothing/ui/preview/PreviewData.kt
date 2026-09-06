package com.radiothing.ui.preview

import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.RadioStation

/**
 * Shared sample data for @Preview composables — never referenced at runtime.
 * Keep every factory pure and side-effect free so previews stay deterministic.
 */

fun previewStation(
    uuid: String = "preview-1",
    name: String = "SomaFM Groove Salad",
    tags: String = "ambient,chill,electronic,downtempo",
    country: String = "United States",
    countryCode: String = "US",
    codec: String = "AAC",
    bitrate: Int = 128,
    votes: Int = 312
) = RadioStation(
    stationUuid = uuid, name = name, url = "", urlResolved = "",
    homepage = "", favicon = "", tags = tags,
    country = country, countryCode = countryCode, language = "english",
    codec = codec, bitrate = bitrate, votes = votes, clickCount = 9200, clickTrend = 0, lastCheckOk = true
)

val previewStations: List<RadioStation> = listOf(
    previewStation(),
    previewStation(uuid = "preview-2", name = "NTS Radio 1", country = "United Kingdom", countryCode = "GB", codec = "MP3", bitrate = 320),
    previewStation(uuid = "preview-3", name = "FIP", tags = "eclectic,jazz", country = "France", countryCode = "FR", codec = "MP3", bitrate = 192, votes = 10432)
)

fun previewPlaylist(id: Long = 1, name: String = "Night Drives") = Playlist(id = id, name = name)

val previewPlaylists: List<Playlist> = listOf(
    previewPlaylist(id = 1, name = "Night Drives"),
    previewPlaylist(id = 2, name = "Focus Lab"),
    previewPlaylist(id = 3, name = "Static")
)
