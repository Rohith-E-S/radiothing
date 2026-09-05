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
    // Start in loading so the screen doesn't flash the empty state before the
    // first Room emission lands
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecentlyPlayedUseCase().collectLatest { history ->
                _uiState.value = _uiState.value.copy(history = history, isLoading = false)
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
            // Snapshot the list at the moment of the tap. The use case flips
            // the persisted state, then the combined flow (recently-played ⊕
            // favorite ids) re-emits and the StateFlow collector updates
            // _uiState. Doing the optimistic flip from a stale local read
            // would race with rapid taps.
            val before = _uiState.value.history.find { it.stationUuid == station.stationUuid }?.isFavorite ?: station.isFavorite
            toggleFavoriteUseCase(station)
            val updatedHistory = _uiState.value.history.map {
                if (it.stationUuid == station.stationUuid) {
                    it.copy(isFavorite = !before)
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
