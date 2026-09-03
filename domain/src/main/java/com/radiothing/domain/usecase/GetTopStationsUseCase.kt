package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationOrder
import com.radiothing.domain.model.StationQuery
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

class GetTopStationsUseCase @Inject constructor(
    private val repository: StationRepository
) {

    suspend operator fun invoke(offset: Int = 0, limit: Int = 20): Result<List<RadioStation>> {
        return repository.getTopStations(offset, limit)
    }

    suspend fun byOrder(order: StationOrder, offset: Int = 0, limit: Int = 20): Result<List<RadioStation>> {
        // topvote only sorts by votes; other orders go through the search endpoint
        return if (order == StationOrder.VOTES) {
            repository.getTopStations(offset, limit)
        } else {
            repository.searchStations(StationQuery(order = order, offset = offset, limit = limit))
        }
    }
}
