package com.radiothing.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onStationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "LOG",
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
                            text = "RECENTLY PLAYED  •  FIELD LOG",
                            color = TextWhite35,
                            fontFamily = Ndot57,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                if (uiState.history.isNotEmpty()) {
                    TextButton(
                        onClick = { showClearConfirm = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = BrightRed),
                        modifier = Modifier.border(1.dp, BrightRed.copy(0.35f), RoundedCornerShape(10.dp))
                    ) {
                        Text("CLEAR", color = BrightRed, fontFamily = Ndot57, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = GridLine, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            if (uiState.history.isEmpty()) {
                EmptyState(type = EmptyStateType.NO_HISTORY, modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
                ) {
                    items(uiState.history, key = { it.stationUuid }) { station ->
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
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("CLEAR LOG?", fontFamily = Ndot57, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp) },
                text = { Text("Remove all recently played stations. Cannot be undone.", fontFamily = Ndot57, fontSize = 12.sp, color = TextWhite35) },
                confirmButton = {
                    TextButton(onClick = {
                        showClearConfirm = false
                        viewModel.clearHistory()
                        scope.launch { snackbarHostState.showSnackbar("Log cleared") }
                    }) { Text("CLEAR", color = BrightRed, fontFamily = Ndot57, fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("CANCEL", fontFamily = Ndot57) } },
                containerColor = Panel,
                titleContentColor = Color.White,
                textContentColor = Color(0xFFAAAAAA),
                shape = RoundedCornerShape(16.dp)
            )
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
