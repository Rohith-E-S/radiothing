package com.radiothing.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.common.EmptyState
import com.radiothing.ui.common.EmptyStateType
import com.radiothing.ui.components.StationListItem
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onStationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {

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
            if (uiState.favorites.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${uiState.favorites.size}",
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

        if (uiState.favorites.isEmpty()) {
            EmptyState(type = EmptyStateType.NO_FAVORITES, modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
            ) {
                items(uiState.favorites, key = { it.stationUuid }) { station ->
                    StationListItem(
                        station = station,
                        isPlaying = false,
                        onStationClick = {
                            viewModel.playStation(station.stationUuid)
                            onStationClick(station.stationUuid)
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(station) }
                    )
                }
            }
        }
    }
}
