package com.radiothing.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.usecase.GetTopStationsUseCase
import com.radiothing.domain.usecase.SearchStationsUseCase
import com.radiothing.domain.usecase.ToggleFavoriteUseCase
import com.radiothing.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getTopStationsUseCase: GetTopStationsUseCase,
    private val searchStationsUseCase: SearchStationsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playerManager: PlayerManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadTopStations()
    }

    private fun loadTopStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val stationsResult = getTopStationsUseCase()
                val stations = stationsResult.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(stations = stations, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            loadTopStations()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val results = searchStationsUseCase(query)
                val stations = results.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(stations = stations, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
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
            // Note: Since BrowseScreen fetches static lists from API, 
            // the favorite state in _uiState.value.stations might not auto-update.
            // A simple local mutation would visually update the UI:
            val updatedStations = _uiState.value.stations.map {
                if (it.stationUuid == station.stationUuid) {
                    it.copy(isFavorite = !it.isFavorite)
                } else it
            }
            _uiState.value = _uiState.value.copy(stations = updatedStations)
        }
    }
}

data class BrowseUiState(
    val searchQuery: String = "",
    val stations: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false
)
