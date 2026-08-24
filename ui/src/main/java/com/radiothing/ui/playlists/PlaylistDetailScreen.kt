package com.radiothing.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.RadioStation
import com.radiothing.ui.components.*

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    viewModel: PlaylistsViewModel,
    onBackClick: () -> Unit,
    onStationClick: (String) -> Unit
) {
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    val playlist by viewModel.currentPlaylistDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp)
    ) {
        Text(
            text = "< BACK",
            color = Color.White,
            fontFamily = Ndot57,
            modifier = Modifier
                .clickable(onClick = onBackClick)
                .padding(bottom = 24.dp)
        )

        playlist?.let { p ->
            Text(
                text = p.name.uppercase(),
                color = Color(0xFFFF2D2D),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Ndot57,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Assuming playlist has a 'stations' property. If not, this logic might need adjustment.
            // For now, it serves as a placeholder structure.
            /* 
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(p.stations) { station ->
                    StationItem(
                        station = station,
                        onClick = { onStationClick(station.id) }
                    )
                }
            } 
            */
            
            Text(
                text = "NO STATIONS AVAILABLE",
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = Ndot57
            )
        }
    }
}

@Composable
fun StationItem(station: RadioStation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Column {
            Text(
                text = station.name.uppercase(),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Ndot57
            )
            Text(
                text = "${station.bitrate}k • ${station.country}",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = Ndot57
            )
        }
    }
}
