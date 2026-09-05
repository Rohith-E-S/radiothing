package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationQuery
import com.radiothing.domain.repository.StationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchStationsUseCaseTest {

    private lateinit var repository: StationRepository
    private lateinit var useCase: SearchStationsUseCase

    private val station = RadioStation(
        stationUuid = "uuid-1",
        name = "Test FM",
        url = "https://example.com/stream",
        urlResolved = "https://example.com/stream",
        homepage = "https://example.com",
        favicon = "",
        tags = "jazz",
        country = "Germany",
        countryCode = "DE",
        language = "german",
        codec = "MP3",
        bitrate = 128,
        votes = 10,
        clickCount = 5,
        clickTrend = 1,
        lastCheckOk = true
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchStationsUseCase(repository)
    }

    // --- parseQuery ---

    @Test
    fun `parseQuery - bare words become name term`() {
        val query = useCase.parseQuery("soma fm")
        assertEquals("soma fm", query.name)
        assertNull(query.tag)
        assertNull(query.country)
        assertNull(query.language)
    }

    @Test
    fun `parseQuery - country prefix is extracted`() {
        val query = useCase.parseQuery("country:germany jazz")
        assertEquals("germany", query.country)
        assertEquals("jazz", query.name)
    }

    @Test
    fun `parseQuery - tag prefix is extracted`() {
        val query = useCase.parseQuery("tag:ambient")
        assertEquals("ambient", query.tag)
        assertNull(query.name)
    }

    @Test
    fun `parseQuery - lang prefix is extracted`() {
        val query = useCase.parseQuery("lang:french news")
        assertEquals("french", query.language)
        assertEquals("news", query.name)
    }

    @Test
    fun `parseQuery - multiple prefixes combine`() {
        val query = useCase.parseQuery("country:japan tag:anime")
        assertEquals("japan", query.country)
        assertEquals("anime", query.tag)
        assertNull(query.name)
    }

    @Test
    fun `parseQuery - empty query yields empty query object`() {
        val query = useCase.parseQuery("   ")
        assertNull(query.name)
        assertNull(query.tag)
        assertNull(query.country)
        assertNull(query.language)
    }

    @Test
    fun `parseQuery - prefix without value is treated as name word`() {
        // "tag:" is exactly 4 chars — fails the length>4 check — becomes a name token
        val query = useCase.parseQuery("tag:")
        assertEquals("tag:", query.name)
    }

    // --- invoke(query: String) ---

    @Test
    fun `invoke returns empty list without any search term`() = runTest {
        val result = useCase("   ")
        assertTrue(result.isSuccess)
        assertEquals(emptyList<RadioStation>(), result.getOrNull())
        coVerify(exactly = 0) { repository.searchStations(any()) }
    }

    @Test
    fun `invoke delegates parsed query to repository`() = runTest {
        coEvery { repository.searchStations(any()) } returns Result.success(listOf(station))

        val result = useCase("country:germany jazz")

        assertTrue(result.isSuccess)
        assertEquals(listOf(station), result.getOrNull())
        val slot = slot<StationQuery>()
        coVerify { repository.searchStations(capture(slot)) }
        assertEquals("germany", slot.captured.country)
        assertEquals("jazz", slot.captured.name)
    }

    @Test
    fun `invoke applies offset and limit to repository call`() = runTest {
        coEvery { repository.searchStations(any()) } returns Result.success(emptyList())

        useCase("tag:ambient", offset = 40, limit = 10)

        val slot = slot<StationQuery>()
        coVerify { repository.searchStations(capture(slot)) }
        assertEquals(40, slot.captured.offset)
        assertEquals(10, slot.captured.limit)
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val error = RuntimeException("network down")
        coEvery { repository.searchStations(any()) } returns Result.failure(error)

        val result = useCase("jazz")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    // --- invoke(query: StationQuery) ---

    @Test
    fun `invoke with StationQuery passes query through unchanged`() = runTest {
        coEvery { repository.searchStations(any()) } returns Result.success(listOf(station))
        val query = StationQuery(name = "test", offset = 5, limit = 50)

        val result = useCase(query)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.searchStations(query) }
    }
}
