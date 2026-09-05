package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.FavoriteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private lateinit var repository: FavoriteRepository
    private lateinit var useCase: ToggleFavoriteUseCase

    private val station = RadioStation(
        stationUuid = "uuid-1",
        name = "Test FM",
        url = "https://example.com/stream",
        urlResolved = "https://example.com/stream",
        homepage = "",
        favicon = "",
        tags = "",
        country = "",
        countryCode = "",
        language = "",
        codec = "MP3",
        bitrate = 128,
        votes = 0,
        clickCount = 0,
        clickTrend = 0,
        lastCheckOk = true
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    fun `adds favorite when station is not favorited`() = runTest {
        coEvery { repository.isFavorite(station.stationUuid) } returns false

        useCase(station)

        coVerify(exactly = 1) { repository.addFavorite(station) }
        coVerify(exactly = 0) { repository.removeFavorite(any()) }
    }

    @Test
    fun `removes favorite when station is already favorited`() = runTest {
        coEvery { repository.isFavorite(station.stationUuid) } returns true

        useCase(station)

        coVerify(exactly = 1) { repository.removeFavorite(station.stationUuid) }
        coVerify(exactly = 0) { repository.addFavorite(any()) }
    }
}
