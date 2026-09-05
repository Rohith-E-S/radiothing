package com.radiothing.data.repository

import com.radiothing.data.api.RadioBrowserApi
import com.radiothing.data.api.dto.CountryDto
import com.radiothing.domain.model.Country
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StationRepositoryImplCatalogCacheTest {

    private lateinit var api: RadioBrowserApi
    private lateinit var repository: StationRepositoryImpl
    private var fakeNow: Long = 1_000_000L

    private val countries = listOf(
        Country("Germany", 1200),
        Country("Japan", 300)
    )

    private val countryDtos = countries.map { CountryDto(it.name, it.stationCount) }

    @Before
    fun setUp() {
        api = mockk()
        repository = StationRepositoryImpl(api).apply { nowMs = { fakeNow } }
    }

    @Test
    fun `first call fetches from api`() = runTest {
        coEvery { api.getCountries() } returns countryDtos

        val result = repository.getCountries()

        assertEquals(countries, result.getOrNull())
        coVerify(exactly = 1) { api.getCountries() }
    }

    @Test
    fun `second call within ttl serves cache without api call`() = runTest {
        coEvery { api.getCountries() } returns countryDtos
        repository.getCountries()

        fakeNow += 1000 // well within 24h TTL
        val result = repository.getCountries()

        assertEquals(countries, result.getOrNull())
        coVerify(exactly = 1) { api.getCountries() }
    }

    @Test
    fun `call after ttl expiry refetches from api`() = runTest {
        coEvery { api.getCountries() } returns countryDtos
        repository.getCountries()

        fakeNow += 24 * 60 * 60 * 1000L + 1 // beyond TTL
        repository.getCountries()

        coVerify(exactly = 2) { api.getCountries() }
    }

    @Test
    fun `failed fetch is not cached - next call retries api`() = runTest {
        coEvery { api.getCountries() } throws RuntimeException("network down") andThen countryDtos

        val first = repository.getCountries()
        assertEquals(true, first.isFailure)

        val second = repository.getCountries()
        assertEquals(countries, second.getOrNull())
        coVerify(exactly = 2) { api.getCountries() }
    }

    @Test
    fun `expired cache with failed refresh serves stale data`() = runTest {
        coEvery { api.getCountries() } returns countryDtos
        repository.getCountries()

        fakeNow += 24 * 60 * 60 * 1000L + 1 // beyond TTL
        coEvery { api.getCountries() } throws RuntimeException("offline")

        val result = repository.getCountries()

        // Stale-but-valid catalog beats an error screen when offline
        assertEquals(countries, result.getOrNull())
    }

    @Test
    fun `stale-served cache is retried on next call after connectivity returns`() = runTest {
        coEvery { api.getCountries() } returns countryDtos
        repository.getCountries()

        fakeNow += 24 * 60 * 60 * 1000L + 1
        coEvery { api.getCountries() } throws RuntimeException("offline") andThen countryDtos
        repository.getCountries() // serves stale

        fakeNow += 24 * 60 * 60 * 1000L + 1
        val refreshed = repository.getCountries()

        assertEquals(countries, refreshed.getOrNull())
        coVerify(exactly = 3) { api.getCountries() }
    }
}
