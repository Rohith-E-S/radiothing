package com.radiothing.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.usecase.GetFavoritesUseCase
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
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playerManager: PlayerManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getFavoritesUseCase().collectLatest { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
    }

    fun playStation(stationUuid: String) {
        val station = _uiState.value.favorites.find { it.stationUuid == stationUuid }
        if (station != null) {
            playerManager.play(station, _uiState.value.favorites)
        }
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            toggleFavoriteUseCase(station)
        }
    }
}

data class FavoritesUiState(
    val favorites: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false
)
