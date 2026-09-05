package com.radiothing.data.repository

import com.radiothing.data.api.RadioBrowserApi
import com.radiothing.domain.model.Country
import com.radiothing.domain.model.Genre
import com.radiothing.domain.model.Language
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationQuery
import com.radiothing.domain.repository.StationRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class StationRepositoryImpl @Inject constructor(
    private val api: RadioBrowserApi
) : StationRepository {

    /** Cached catalog entry with a fetch timestamp for TTL expiry. */
    private data class CachedEntry<T>(val value: T, val fetchedAtMs: Long)

    private suspend fun <T> cached(
        cache: CachedEntry<T>?,
        ttlMs: Long = CATALOG_CACHE_TTL_MS,
        fetch: suspend () -> T
    ): Pair<Result<T>, CachedEntry<T>?> {
        val now = nowMs()
        if (cache != null && now - cache.fetchedAtMs < ttlMs) {
            return Result.success(cache.value) to cache
        }
        return try {
            val value = fetch()
            Result.success(value) to CachedEntry(value, now)
        } catch (e: Exception) {
            Result.failure<T>(e) to null
        }
    }

    private var countriesCache: CachedEntry<List<Country>>? = null
    private var genresCache: CachedEntry<List<Genre>>? = null
    private var languagesCache: CachedEntry<List<Language>>? = null
    private val catalogMutex = Mutex()

    /** Injectable clock for tests; production uses wall time. */
    internal var nowMs: () -> Long = { System.currentTimeMillis() }

    override suspend fun searchStations(query: StationQuery): Result<List<RadioStation>> {
        return try {
            val response = api.searchStations(
                name = query.name,
                tag = query.tag,
                country = query.country,
                language = query.language,
                order = query.order.apiValue,
                reverse = query.order == com.radiothing.domain.model.StationOrder.VOTES ||
                    query.order == com.radiothing.domain.model.StationOrder.BITRATE ||
                    query.order == com.radiothing.domain.model.StationOrder.CLICKS,
                offset = query.offset,
                limit = query.limit
            )
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

    override suspend fun getCountries(): Result<List<Country>> = catalogMutex.withLock {
        val (result, entry) = cached(countriesCache) {
            api.getCountries().map { it.toDomain() }
        }
        if (entry != null) countriesCache = entry
        result
    }

    override suspend fun getGenres(): Result<List<Genre>> = catalogMutex.withLock {
        val (result, entry) = cached(genresCache) {
            api.getTags().map { it.toDomain() }
        }
        if (entry != null) genresCache = entry
        result
    }

    override suspend fun getLanguages(): Result<List<Language>> = catalogMutex.withLock {
        val (result, entry) = cached(languagesCache) {
            api.getLanguages().map { it.toDomain() }
        }
        if (entry != null) languagesCache = entry
        result
    }

    override suspend fun clickStation(stationUuid: String) {
        try {
            api.clickStation(stationUuid)
        } catch (e: Exception) {
            // Ignore click tracking failures
        }
    }

    companion object {
        /** Country/language/tag catalogs change rarely — cache for 24h. */
        private const val CATALOG_CACHE_TTL_MS = 24 * 60 * 60 * 1000L
    }
}
