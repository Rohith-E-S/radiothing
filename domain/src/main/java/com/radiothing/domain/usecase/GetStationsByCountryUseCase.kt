package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

class GetStationsByCountryUseCase @Inject constructor(
    private val repository: StationRepository
) {
    suspend operator fun invoke(country: String, offset: Int = 0, limit: Int = 20): Result<List<RadioStation>> {
        return repository.getStationsByCountry(country, offset, limit)
    }
}
