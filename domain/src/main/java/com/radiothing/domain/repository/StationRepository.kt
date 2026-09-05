package com.radiothing.domain.repository

import com.radiothing.domain.model.Country
import com.radiothing.domain.model.Genre
import com.radiothing.domain.model.Language
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationQuery

/**
 * Read access to the Radio Browser community catalog.
 *
 * All station lookups return [Result] so callers decide how to surface
 * network failures — repository never throws for expected API errors.
 */
interface StationRepository {
    /** Multi-field search driven by [StationQuery]. */
    suspend fun searchStations(query: StationQuery): Result<List<RadioStation>>
    suspend fun getStationsByCountry(country: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getStationsByGenre(tag: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getStationsByLanguage(language: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getTopStations(offset: Int, limit: Int): Result<List<RadioStation>>

    /** Country/language/tag catalogs — slow-changing; implementations may cache. */
    suspend fun getCountries(): Result<List<Country>>
    suspend fun getGenres(): Result<List<Genre>>
    suspend fun getLanguages(): Result<List<Language>>

    /** Reports a listen back to the community catalog. Best-effort — never throws. */
    suspend fun clickStation(stationUuid: String)
}
