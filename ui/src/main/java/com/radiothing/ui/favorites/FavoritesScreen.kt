package com.radiothing.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.PlayerState
import com.radiothing.player.PlayerManager
import com.radiothing.ui.common.EmptyState
import com.radiothing.ui.common.EmptyStateType
import com.radiothing.ui.components.StationListItem
import com.radiothing.ui.components.StationListSkeleton
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ndot57
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.RadioThingTheme
import com.radiothing.ui.common.LocalBottomClearance
import com.radiothing.ui.preview.previewStations
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    playerManager: PlayerManager,
    onStationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerStateState = playerManager.playerState.collectAsStateWithLifecycle()

    FavoritesContent(
        isLoading = uiState.isLoading,
        favorites = uiState.favorites,
        playerStateState = playerStateState,
        onStationClick = { uuid ->
            viewModel.playStation(uuid)
            onStationClick(uuid)
        },
        onToggleFavoriteForUndo = viewModel::toggleFavoriteForUndo,
        onRestoreFavorite = viewModel::restoreFavorite
    )
}

/** Stateless body of the favorites screen — hoisted out so it can be previewed without a ViewModel. */
@Composable
private fun FavoritesContent(
    isLoading: Boolean,
    favorites: List<RadioStation>,
    playerStateState: State<PlayerState>,
    onStationClick: (String) -> Unit,
    onToggleFavoriteForUndo: (RadioStation) -> RadioStation?,
    onRestoreFavorite: (RadioStation) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Undo helper — 4s window to restore an accidental unfavorite
    fun unfavoriteWithUndo(station: RadioStation) {
        val removed = onToggleFavoriteForUndo(station)
        if (removed != null) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "${removed.name.uppercase()} DROPPED",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onRestoreFavorite(removed)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // header + list (as before)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "FAVORITES",
                    color = Color.White,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 2.5.sp
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).clip(RoundedCornerShape(100.dp)).background(BrightRed))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "YOUR COLLECTION  •  SAVED SIGNALS",
                        color = TextWhite35,
                        fontFamily = Ndot57,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            if (favorites.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${favorites.size}",
                        color = BrightRed,
                        fontFamily = Ndot57,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text("SAVED", color = TextWhite35, fontFamily = Ndot57, fontSize = 9.sp, letterSpacing = 1.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = GridLine, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        when {
            // Loading skeleton until the first Room emission — an empty list
            // before that is "not loaded yet", not "no favorites"
            isLoading -> StationListSkeleton(Modifier.fillMaxSize().padding(bottom = LocalBottomClearance.current))
            favorites.isEmpty() -> EmptyState(type = EmptyStateType.NO_FAVORITES, modifier = Modifier.fillMaxSize().padding(bottom = LocalBottomClearance.current))
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = LocalBottomClearance.current, top = 4.dp)
            ) {
                items(favorites, key = { it.stationUuid }, contentType = { "station" }) { station ->
                    val isPlaying by remember(station.stationUuid) {
                        derivedStateOf {
                            val ps = playerStateState.value
                            ps.currentStation?.stationUuid == station.stationUuid && ps.isPlaying
                        }
                    }
                    val stationClick = remember(station.stationUuid) { { onStationClick(station.stationUuid) } }
                    // Keyed on the whole station: a flow refresh can replace the
                    // object for the same uuid (e.g. updated isFavorite), and the
                    // lambda must not act on the stale instance
                    val favClick = remember(station) { { unfavoriteWithUndo(station) } }
                    StationListItem(
                        station = station,
                        isPlaying = isPlaying,
                        onStationClick = stationClick,
                        onFavoriteClick = favClick
                    )
                }
            }
        }
    }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LocalBottomClearance.current + 12.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000L, device = "spec:width=412dp,height=915dp", name = "Loaded")
@Composable
private fun FavoritesScreenLoadedPreview() {
    RadioThingTheme {
        FavoritesContent(
            isLoading = false,
            favorites = previewStations,
            playerStateState = remember {
                mutableStateOf(PlayerState(currentStation = previewStations[0], isPlaying = true))
            },
            onStationClick = {},
            onToggleFavoriteForUndo = { it },
            onRestoreFavorite = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000L, device = "spec:width=412dp,height=915dp", name = "Empty")
@Composable
private fun FavoritesScreenEmptyPreview() {
    RadioThingTheme {
        FavoritesContent(
            isLoading = false,
            favorites = emptyList(),
            playerStateState = remember { mutableStateOf(PlayerState()) },
            onStationClick = {},
            onToggleFavoriteForUndo = { null },
            onRestoreFavorite = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000L, device = "spec:width=412dp,height=915dp", name = "Loading skeleton")
@Composable
private fun FavoritesScreenLoadingPreview() {
    RadioThingTheme {
        FavoritesContent(
            isLoading = true,
            favorites = emptyList(),
            playerStateState = remember { mutableStateOf(PlayerState()) },
            onStationClick = {},
            onToggleFavoriteForUndo = { null },
            onRestoreFavorite = {}
        )
    }
}
