package com.radiothing.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.repository.FavoriteRepository
import com.radiothing.domain.usecase.GetTopStationsUseCase
import com.radiothing.domain.usecase.SearchStationsUseCase
import com.radiothing.domain.usecase.ToggleFavoriteUseCase
import com.radiothing.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getTopStationsUseCase: GetTopStationsUseCase,
    private val searchStationsUseCase: SearchStationsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playerManager: PlayerManager,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadTopStations()
        // Keep isFavorite in sync if user favorites from NowPlaying/player
        viewModelScope.launch {
            favoriteRepository.getFavoriteIds().collect { ids ->
                val enrichedUnfiltered = _uiState.value.unfilteredStations.map { it.copy(isFavorite = ids.contains(it.stationUuid)) }
                _uiState.value = _uiState.value.copy(unfilteredStations = enrichedUnfiltered)
                applyFiltersInternal()
            }
        }
    }

    private suspend fun enrichWithFavorites(stations: List<RadioStation>): List<RadioStation> {
        return try {
            val ids = favoriteRepository.getFavoriteIds().first()
            stations.map { it.copy(isFavorite = ids.contains(it.stationUuid)) }
        } catch (_: Exception) { stations }
    }

    private fun loadTopStations(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            else _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val stationsResult = getTopStationsUseCase()
                val stations = enrichWithFavorites(stationsResult.getOrElse { throw it })
                _uiState.value = _uiState.value.copy(unfilteredStations = stations, isLoading = false, isRefreshing = false, hasSearched = false, canLoadMore = stations.size >= 20, currentOffset = 0, error = null)
                applyFiltersInternal()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load stations")
            }
        }
    }

    fun refresh() {
        val q = _uiState.value.searchQuery
        if (q.isBlank()) loadTopStations(refresh = true) else performSearch(refresh = true)
    }

    fun retry() {
        val q = _uiState.value.searchQuery
        if (q.isBlank() || !_uiState.value.hasSearched) loadTopStations() else performSearch()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun performSearch(refresh: Boolean = false) {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            loadTopStations(refresh = refresh)
            return
        }
        viewModelScope.launch {
            if (refresh) _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            else _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = searchStationsUseCase(query)
                val stations = enrichWithFavorites(results.getOrElse { throw it })
                _uiState.value = _uiState.value.copy(unfilteredStations = stations, isLoading = false, isRefreshing = false, hasSearched = true, error = null)
                applyFiltersInternal()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Search failed")
            }
        }
    }

    fun applyFilters(bitrates: Set<String>, codecs: Set<String>) {
        _uiState.value = _uiState.value.copy(selectedBitrates = bitrates, selectedCodecs = codecs)
        applyFiltersInternal()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(selectedBitrates = emptySet(), selectedCodecs = emptySet())
        applyFiltersInternal()
    }

    private fun applyFiltersInternal() {
        val state = _uiState.value
        var list = state.unfilteredStations
        if (state.selectedBitrates.isNotEmpty()) {
            list = list.filter { station -> state.selectedBitrates.contains(station.bitrate.toString()) }
        }
        if (state.selectedCodecs.isNotEmpty()) {
            val lower = state.selectedCodecs.map { it.lowercase() }.toSet()
            list = list.filter { lower.contains(it.codec.lowercase()) }
        }
        _uiState.value = _uiState.value.copy(stations = list)
    }

    fun loadMore() {
        // Placeholder for pagination when API supports offset; trigger re-search with larger limit later
    }


    fun playStation(stationUuid: String) {
        val station = _uiState.value.stations.find { it.stationUuid == stationUuid }
        if (station != null) {
            playerManager.play(station, _uiState.value.stations)
        }
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            toggleFavoriteUseCase(station)
            // Immediate flip for both master and filtered lists (DB flow will confirm)
            val newFav = !station.isFavorite
            val updatedUnfiltered = _uiState.value.unfilteredStations.map {
                if (it.stationUuid == station.stationUuid) it.copy(isFavorite = newFav) else it
            }
            _uiState.value = _uiState.value.copy(unfilteredStations = updatedUnfiltered)
            applyFiltersInternal()
        }
    }
}

data class BrowseUiState(
    val searchQuery: String = "",
    val stations: List<RadioStation> = emptyList(),
    val unfilteredStations: List<RadioStation> = emptyList(),
    val selectedBitrates: Set<String> = emptySet(),
    val selectedCodecs: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentOffset: Int = 0
)
