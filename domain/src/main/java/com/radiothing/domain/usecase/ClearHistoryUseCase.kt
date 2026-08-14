package com.radiothing.domain.usecase

import com.radiothing.domain.repository.RecentlyPlayedRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val recentlyPlayedRepository: RecentlyPlayedRepository
) {
    suspend operator fun invoke() {
        recentlyPlayedRepository.clearHistory()
    }
}
