package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(station: RadioStation) {
        if (favoriteRepository.isFavorite(station.stationUuid)) {
            favoriteRepository.removeFavorite(station.stationUuid)
        } else {
            favoriteRepository.addFavorite(station)
        }
    }
}
