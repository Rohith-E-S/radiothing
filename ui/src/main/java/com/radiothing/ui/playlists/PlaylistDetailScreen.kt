package com.radiothing.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.RadioStation
import com.radiothing.player.PlayerManager
import com.radiothing.ui.components.StationListSkeleton
import com.radiothing.ui.components.DotMatrixIcon
import com.radiothing.ui.components.IconType
import com.radiothing.ui.components.StationListItem
import com.radiothing.ui.common.EmptyState
import com.radiothing.ui.common.EmptyStateType
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ndot57
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    viewModel: PlaylistsViewModel,
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onStationClick: (String) -> Unit
) {
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    val details by viewModel.currentPlaylistDetails.collectAsStateWithLifecycle()
    val playerStateState = playerManager.playerState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp)
    ) {
        // Header — 48dp back, title, live count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = details?.playlist?.name?.uppercase() ?: "TRAY",
                    color = Color.White,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
                val count = details?.stations?.size ?: 0
                Text(
                    text = "$count SPECIMENS",
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
            // Play-all — starts the whole tray as a queue
            if ((details?.stations?.size ?: 0) > 0) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(BrightRed),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        val stations = details?.stations ?: emptyList()
                        if (stations.isNotEmpty()) {
                            playerManager.play(stations[0], stations, 0)
                            onStationClick(stations[0].stationUuid)
                        }
                    }) {
                        DotMatrixIcon(type = IconType.PLAY, size = 20.dp, color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val stations = details?.stations ?: emptyList()
        when {
            // details == null means the flow hasn't emitted yet — show a loading
            // skeleton, not a misleading "empty playlist" state
            details == null -> StationListSkeleton(Modifier.fillMaxSize().padding(bottom = 100.dp))
            stations.isEmpty() -> EmptyState(type = EmptyStateType.NO_RESULTS, modifier = Modifier.fillMaxSize().padding(bottom = 100.dp))
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 140.dp, top = 4.dp)
            ) {
                itemsIndexed(stations, key = { _, s -> s.stationUuid }, contentType = { _, _ -> "station" }) { _, station ->
                    val isPlaying by androidx.compose.runtime.remember(station.stationUuid) {
                        androidx.compose.runtime.derivedStateOf {
                            val ps = playerStateState.value
                            ps.currentStation?.stationUuid == station.stationUuid && ps.isPlaying
                        }
                    }
                    // Keyed on the whole station (plus the list): a flow refresh can
                    // replace the object for the same uuid, and play() must receive
                    // the fresh instance, not a stale one
                    val stationClick = androidx.compose.runtime.remember(station, stations) {
                        {
                            val idx = stations.indexOfFirst { it.stationUuid == station.stationUuid }
                            playerManager.play(station, stations, idx.coerceAtLeast(0))
                            onStationClick(station.stationUuid)
                        }
                    }
                    val favClick = androidx.compose.runtime.remember(station) { { viewModel.toggleFavorite(station) } }
                    StationListItem(
                        station = station,
                        isPlaying = isPlaying,
                        onStationClick = stationClick,
                        onFavoriteClick = favClick,
                        // Own lane inside the card's top row — the old overlay
                        // Box covered the heart's 48dp touch target and won
                        // pointer input over most of it
                        trailingTopContent = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable(onClickLabel = "Remove from playlist") {
                                        viewModel.removeStationFromPlaylist(playlistId, station.stationUuid)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove from playlist",
                                    tint = TextWhite35,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
