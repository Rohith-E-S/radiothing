package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

class GetStationsByLanguageUseCase @Inject constructor(
    private val repository: StationRepository
) {
    suspend operator fun invoke(language: String, offset: Int = 0, limit: Int = 20): Result<List<RadioStation>> {
        return repository.getStationsByLanguage(language, offset, limit)
    }
}
