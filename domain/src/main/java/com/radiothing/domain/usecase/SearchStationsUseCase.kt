package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

class SearchStationsUseCase @Inject constructor(
    private val repository: StationRepository
) {
    suspend operator fun invoke(query: String, offset: Int = 0, limit: Int = 20): Result<List<RadioStation>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchStations(query, offset, limit)
    }
}
