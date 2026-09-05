package com.radiothing.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.Playlist
import com.radiothing.ui.common.EmptyState
import com.radiothing.ui.common.EmptyStateType
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Hairline
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.PureBlack
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70
import com.radiothing.ui.common.LocalBottomClearance

@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel,
    onPlaylistClick: (Long) -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()
    val counts by viewModel.playlistCounts.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Playlist?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text(
                    text = "PLAYLISTS",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Ndot57,
                    letterSpacing = 2.5.sp
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).clip(RoundedCornerShape(100.dp)).background(BrightRed))
                    Spacer(Modifier.width(6.dp))
                    Text("${playlists.size} COLLECTIONS", color = TextWhite35, fontFamily = Ndot57, fontSize = 9.sp, letterSpacing = 1.sp)
                }
            }
            Button(
                onClick = { showCreate = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrightRed),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("+ NEW", fontFamily = Ndot57, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = GridLine, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        if (playlists.isEmpty()) {
            EmptyState(type = EmptyStateType.NO_PLAYLISTS, modifier = Modifier.fillMaxSize().padding(bottom = LocalBottomClearance.current))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = LocalBottomClearance.current, top = 4.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        count = counts[playlist.id] ?: 0,
                        onClick = { onPlaylistClick(playlist.id) },
                        onDelete = { deleteTarget = playlist }
                    )
                }
            }
        }
        if (showCreate) {
            AlertDialog(
                onDismissRequest = { showCreate = false },
                title = { Text("NEW PLAYLIST", fontFamily = Ndot57, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Name — e.g. NIGHT DRIVES", fontFamily = Ndot57, fontSize = 12.sp, color = TextWhite35) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightRed,
                            unfocusedBorderColor = GridLine,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BrightRed
                        ),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank()) viewModel.createPlaylist(newName.trim())
                            newName = ""
                            showCreate = false
                        },
                        enabled = newName.isNotBlank()
                    ) { Text("CREATE", fontFamily = Ndot57, fontWeight = FontWeight.Bold, color = BrightRed) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Reset on cancel so the next open starts fresh
                            newName = ""
                            showCreate = false
                        }
                    ) { Text("CANCEL", fontFamily = Ndot57) }
                },
                containerColor = Panel,
                titleContentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
        // Delete confirmation — destructive, so confirm before dropping the tray
        deleteTarget?.let { playlist ->
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("DELETE TRAY?", fontFamily = Ndot57, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp) },
                text = { Text("\"${playlist.name.uppercase()}\" and its ${counts[playlist.id] ?: 0} specimens will be removed. Cannot be undone.", fontFamily = Ndot57, fontSize = 12.sp, color = TextWhite35) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePlaylist(playlist.id)
                            deleteTarget = null
                        }
                    ) { Text("DELETE", fontFamily = Ndot57, fontWeight = FontWeight.Bold, color = BrightRed) }
                },
                dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("CANCEL", fontFamily = Ndot57) } },
                containerColor = Panel,
                titleContentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun PlaylistItem(playlist: Playlist, count: Int = 0, onClick: () -> Unit, onDelete: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, GridLine, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tape / ledger visual
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0D0D0F))
                .border(1.dp, Hairline, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = playlist.name.take(2).uppercase(),
                color = BrightRed,
                fontSize = 14.sp,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name.uppercase(),
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$count SPECIMENS",
                color = TextWhite35,
                fontSize = 10.sp,
                fontFamily = Ndot57,
                letterSpacing = 0.6.sp
            )
        }
        if (count > 0) {
            Text(
                text = "›",
                color = TextWhite35,
                fontSize = 18.sp,
                fontFamily = Ndot57
            )
        }
        Spacer(Modifier.width(8.dp))
        // Delete — trailing trash, 48dp touch
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .clickable(onClickLabel = "Delete playlist", onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete playlist",
                tint = TextWhite35,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
