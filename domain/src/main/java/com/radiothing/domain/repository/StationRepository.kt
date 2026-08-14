package com.radiothing.domain.repository

import com.radiothing.domain.model.Country
import com.radiothing.domain.model.Genre
import com.radiothing.domain.model.Language
import com.radiothing.domain.model.RadioStation

interface StationRepository {
    suspend fun searchStations(query: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getStationsByCountry(country: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getStationsByGenre(tag: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getStationsByLanguage(language: String, offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getTopStations(offset: Int, limit: Int): Result<List<RadioStation>>
    suspend fun getCountries(): Result<List<Country>>
    suspend fun getGenres(): Result<List<Genre>>
    suspend fun getLanguages(): Result<List<Language>>
    suspend fun clickStation(stationUuid: String)
}
