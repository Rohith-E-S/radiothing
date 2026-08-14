package com.radiothing.data.repository

import com.radiothing.data.api.RadioBrowserApi
import com.radiothing.domain.model.Country
import com.radiothing.domain.model.Genre
import com.radiothing.domain.model.Language
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

class StationRepositoryImpl @Inject constructor(
    private val api: RadioBrowserApi
) : StationRepository {

    override suspend fun searchStations(query: String, offset: Int, limit: Int): Result<List<RadioStation>> {
        return try {
            val response = api.searchStations(name = query, offset = offset, limit = limit)
            Result.success(response.map { it.toRadioStation() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStationsByCountry(country: String, offset: Int, limit: Int): Result<List<RadioStation>> {
        return try {
            val response = api.getStationsByCountry(country, offset, limit)
            Result.success(response.map { it.toRadioStation() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStationsByGenre(tag: String, offset: Int, limit: Int): Result<List<RadioStation>> {
        return try {
            val response = api.getStationsByGenre(tag, offset, limit)
            Result.success(response.map { it.toRadioStation() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStationsByLanguage(language: String, offset: Int, limit: Int): Result<List<RadioStation>> {
        return try {
            val response = api.getStationsByLanguage(language, offset, limit)
            Result.success(response.map { it.toRadioStation() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopStations(offset: Int, limit: Int): Result<List<RadioStation>> {
        return try {
            val response = api.getTopStations(offset = offset, limit = limit)
            Result.success(response.map { it.toRadioStation() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCountries(): Result<List<Country>> {
        return try {
            val response = api.getCountries()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val response = api.getTags()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLanguages(): Result<List<Language>> {
        return try {
            val response = api.getLanguages()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clickStation(stationUuid: String) {
        try {
            api.clickStation(stationUuid)
        } catch (e: Exception) {
            // Ignore click tracking failures
        }
    }
}
