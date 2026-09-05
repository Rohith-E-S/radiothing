package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.Playlist
import com.radiothing.domain.model.RadioStation
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Ndot57
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

/**
 * Add-to-playlist picker — "SEND TO TRAY". Shows all playlists with live counts,
 * creates a new one inline, adds the station on tap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    station: RadioStation,
    playlists: List<Playlist>,
    counts: Map<Long, Int>,
    onAddTo: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newPlaylistName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0A0C),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("SEND TO TRAY", color = Color.White, fontFamily = Ndot57, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.5.sp)
            Text(
                station.name.uppercase(),
                color = TextWhite35,
                fontFamily = Ndot57,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(16.dp))

            if (playlists.isEmpty()) {
                Text("NO TRAYS YET — NAME ONE BELOW", color = TextWhite35, fontFamily = Ndot57, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        val count = counts[playlist.id] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(100.dp))
                                .background(Panel)
                                .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                                .clickable { onAddTo(playlist.id) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Ink)
                                    .border(1.dp, GridLine, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    playlist.name.take(2).uppercase(),
                                    color = BrightRed,
                                    fontFamily = Ndot57,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                playlist.name.uppercase(),
                                color = Color.White,
                                fontFamily = Ndot57,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text("$count", color = TextWhite35, fontFamily = Ndot57, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Inline new-tray row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .background(Ink)
                    .border(1.dp, if (newPlaylistName.isNotBlank()) BrightRed else GridLine, RoundedCornerShape(100.dp))
                    .clickable(enabled = newPlaylistName.isNotBlank()) {
                        onCreateAndAdd(newPlaylistName.trim())
                        newPlaylistName = "" // reset so the same name isn't added twice
                    }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NothingTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = "NEW TRAY — NAME IT",
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (newPlaylistName.isNotBlank()) BrightRed else GridLine)
                        .clickable(enabled = newPlaylistName.isNotBlank()) {
                            onCreateAndAdd(newPlaylistName.trim())
                            newPlaylistName = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create playlist and add station", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
