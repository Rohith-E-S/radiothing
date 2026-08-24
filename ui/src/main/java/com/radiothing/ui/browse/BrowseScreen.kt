package com.radiothing.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.radiothing.ui.common.EmptyState
import com.radiothing.ui.common.EmptyStateType
import com.radiothing.ui.common.ErrorState
import com.radiothing.ui.components.FilterSheet
import com.radiothing.ui.components.NothingTextField
import com.radiothing.ui.components.StationListItem
import com.radiothing.ui.components.StationListSkeleton
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onStationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 16.dp)
            // pull 16dp into the status-bar inset — leaves ~8-10dp gap under icons instead of 28-32dp
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {

        // ── Header: tight instrument plate (saves ~26dp vs before) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BROWSE",
                    color = Color.White,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.width(10.dp))
                Box(Modifier.size(4.dp).clip(RoundedCornerShape(100.dp)).background(BrightRed))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "RADIO • BROWSER • OPEN",
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                )
            }
            if (uiState.stations.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${uiState.stations.size}",
                        color = BrightRed,
                        fontFamily = Ndot57,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.8.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "TUNED",
                        color = TextWhite35,
                        fontFamily = Ndot57,
                        fontSize = 8.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Search enclosure ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NothingTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "SEARCH — NAME, COUNTRY, TAG…",
                onSearch = {
                    viewModel.performSearch()
                    keyboardController?.hide()
                },
                modifier = Modifier.weight(1f)
            )
            // Filter: pill button 48dp
            IconButton(
                onClick = { showFilters = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Panel)
                    .border(1.dp, Hairline, RoundedCornerShape(100.dp))
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Active filter hint — compact single line, divider tight
        if (uiState.searchQuery.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUERY “${uiState.searchQuery.uppercase().take(24)}”",
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                TextButton(
                    onClick = { viewModel.onSearchQueryChange(""); viewModel.performSearch() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("CLEAR", color = BrightRed, fontFamily = Ndot57, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = GridLine, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
        } else {
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = GridLine, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
        }

        // Active filters — pill row, multiselect (any number of bitrates + codecs)
        if (uiState.selectedBitrates.isNotEmpty() || uiState.selectedCodecs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FILTERS:", color = TextWhite35, fontFamily = Ndot57, fontSize = 9.sp, letterSpacing = 0.8.sp)
                uiState.selectedBitrates.forEach { br ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(BrightRed.copy(alpha = 0.14f))
                            .border(1.dp, BrightRed, RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("${br}K", color = BrightRed, fontFamily = Ndot57, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
                uiState.selectedCodecs.forEach { cd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(BrightRed.copy(alpha = 0.14f))
                            .border(1.dp, BrightRed, RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text(cd.uppercase(), color = BrightRed, fontFamily = Ndot57, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { viewModel.clearFilters() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("CLEAR", color = BrightRed, fontFamily = Ndot57, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (showFilters) {
            FilterSheet(
                onApply = { bitrates, codecs ->
                    viewModel.applyFilters(bitrates, codecs)
                    showFilters = false
                },
                onClear = {
                    viewModel.clearFilters()
                    showFilters = false
                },
                onDismiss = { showFilters = false },
                initialBitrates = uiState.selectedBitrates,
                initialCodecs = uiState.selectedCodecs
            )
        }

        // ── Content ──
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    StationListSkeleton(modifier = Modifier.fillMaxSize().padding(top = 4.dp))
                }
                uiState.error != null -> {
                    ErrorState(message = uiState.error ?: "Unknown error", onRetry = { viewModel.retry() })
                }
                uiState.stations.isEmpty() -> {
                    EmptyState(type = EmptyStateType.NO_RESULTS, modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
                    ) {
                        items(uiState.stations, key = { it.stationUuid }) { station ->
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
            if (uiState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Ink)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrightRed)
                        Spacer(Modifier.width(8.dp))
                        Text("TUNING…", color = TextWhite70, fontFamily = Ndot57, fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}
