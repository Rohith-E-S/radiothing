package com.radiothing.data.repository

import androidx.room.withTransaction
import com.radiothing.data.db.RadioDatabase
import com.radiothing.data.db.dao.FavoriteDao
import com.radiothing.data.db.dao.PlaylistDao
import com.radiothing.data.db.dao.PlaylistStationCount
import com.radiothing.data.db.entity.PlaylistEntity
import com.radiothing.data.db.entity.PlaylistStationEntity
import com.radiothing.domain.model.RadioStation
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlaylistRepositoryImplTest {

    private lateinit var dao: PlaylistDao
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var database: RadioDatabase
    private lateinit var repository: PlaylistRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        favoriteDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        repository = PlaylistRepositoryImpl(dao, favoriteDao, database)
        // Execute withTransaction blocks directly so repository logic runs in tests
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction(any<suspend () -> Unit>()) } coAnswers {
            // args[0] is the receiver (the database); args[1] is the block
            @Suppress("UNCHECKED_CAST")
            (args[1] as suspend () -> Unit).invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun station(uuid: String = "uuid-1") = RadioStation(
        stationUuid = uuid,
        name = "Test Radio",
        url = "http://example.com/stream",
        urlResolved = "http://example.com/stream",
        homepage = "",
        favicon = "",
        tags = "pop",
        country = "Germany",
        countryCode = "DE",
        language = "english",
        codec = "MP3",
        bitrate = 128,
        votes = 10,
        clickCount = 5,
        clickTrend = 0,
        lastCheckOk = true,
        isFavorite = false
    )

    @Test
    fun `deletePlaylist removes stations and playlist through transactional dao call`() = runTest {
        repository.deletePlaylist(42L)

        coVerify(exactly = 1) { dao.deletePlaylistWithStations(42L) }
        coVerify(exactly = 0) { dao.deletePlaylist(any()) }
    }

    @Test
    fun `addStationToPlaylist inserts with orderIndex from count and bumps updatedAt`() = runTest {
        coEvery { dao.stationExists(1L, "uuid-1") } returns 0
        coEvery { dao.getStationCount(1L) } returns 3

        repository.addStationToPlaylist(1L, station("uuid-1"))

        val inserted = slot<PlaylistStationEntity>()
        coVerify(exactly = 1) { dao.insertPlaylistStation(capture(inserted)) }
        assertEquals(3, inserted.captured.orderIndex)
        coVerify(exactly = 1) { dao.touchPlaylist(1L, any()) }
    }

    @Test
    fun `addStationToPlaylist for existing station does not reinsert or move to end`() = runTest {
        coEvery { dao.stationExists(1L, "uuid-1") } returns 1

        repository.addStationToPlaylist(1L, station("uuid-1"))

        coVerify(exactly = 0) { dao.insertPlaylistStation(any()) }
        coVerify(exactly = 1) { dao.touchPlaylist(1L, any()) }
    }

    @Test
    fun `removeStationFromPlaylist removes and bumps updatedAt`() = runTest {
        repository.removeStationFromPlaylist(1L, "uuid-1")

        coVerify(exactly = 1) { dao.removePlaylistStation(1L, "uuid-1") }
        coVerify(exactly = 1) { dao.touchPlaylist(1L, any()) }
    }

    @Test
    fun `getPlaylistStationCounts maps dao rows to playlistId to count map`() = runTest {
        coEvery { dao.getPlaylistStationCounts() } returns flowOf(
            listOf(
                PlaylistStationCount(1L, 3),
                PlaylistStationCount(2L, 0)
            )
        )

        val counts = repository.getPlaylistStationCounts()

        assertEquals(mapOf(1L to 3, 2L to 0), counts.first())
    }

    @Test
    fun `getPlaylistWithStations marks stations present in favorites`() = runTest {
        val entity = PlaylistEntity(id = 7L, name = "Chill", description = "", createdAt = 1L, updatedAt = 1L)
        val stationEntity = station("uuid-1").let {
            PlaylistStationEntity(
                playlistId = 7L,
                stationUuid = it.stationUuid,
                name = it.name,
                url = it.url,
                urlResolved = it.urlResolved,
                homepage = it.homepage,
                favicon = it.favicon,
                tags = it.tags,
                country = it.country,
                countryCode = it.countryCode,
                language = it.language,
                codec = it.codec,
                bitrate = it.bitrate,
                votes = it.votes,
                clickCount = it.clickCount,
                clickTrend = it.clickTrend,
                lastCheckOk = it.lastCheckOk,
                orderIndex = 0
            )
        }

        coEvery { dao.getPlaylistById(7L) } returns flowOf(entity)
        coEvery { dao.getPlaylistStations(7L) } returns flowOf(listOf(stationEntity))
        coEvery { favoriteDao.getAllIds() } returns flowOf(listOf("uuid-1"))

        val result = repository.getPlaylistWithStations(7L).first()

        assertTrue(result != null)
        assertEquals(7L, result!!.playlist.id)
        assertEquals(1, result.stations.size)
        assertTrue(result.stations[0].isFavorite)
    }
}
