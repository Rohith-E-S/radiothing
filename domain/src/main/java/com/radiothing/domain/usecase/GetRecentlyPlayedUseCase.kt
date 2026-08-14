package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.RecentlyPlayedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyPlayedUseCase @Inject constructor(
    private val recentlyPlayedRepository: RecentlyPlayedRepository
) {
    operator fun invoke(): Flow<List<RadioStation>> {
        return recentlyPlayedRepository.getRecentlyPlayed()
    }
}
