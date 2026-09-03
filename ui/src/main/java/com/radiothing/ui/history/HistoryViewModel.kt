package com.radiothing.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.usecase.ClearHistoryUseCase
import com.radiothing.domain.usecase.GetRecentlyPlayedUseCase
import com.radiothing.domain.usecase.ToggleFavoriteUseCase
import com.radiothing.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playerManager: PlayerManager,
    private val stationRepository: com.radiothing.domain.repository.StationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            getRecentlyPlayedUseCase().collectLatest { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }

    fun playStation(stationUuid: String) {
        val station = _uiState.value.history.find { it.stationUuid == stationUuid }
        if (station != null) {
            playerManager.play(station, _uiState.value.history)
            // Feed the community catalog's click counters
            viewModelScope.launch { try { stationRepository.clickStation(stationUuid) } catch (_: Exception) {} }
        }
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            toggleFavoriteUseCase(station)
            val updatedHistory = _uiState.value.history.map {
                if (it.stationUuid == station.stationUuid) {
                    it.copy(isFavorite = !it.isFavorite)
                } else it
            }
            _uiState.value = _uiState.value.copy(history = updatedHistory)
        }
    }
}

data class HistoryUiState(
    val history: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false
)
