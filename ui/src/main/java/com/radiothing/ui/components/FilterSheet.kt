package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.RadioColors
import com.radiothing.ui.theme.RadioShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    onApply: (Set<String>, Set<String>) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    initialBitrates: Set<String> = emptySet(),
    initialCodecs: Set<String> = emptySet()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedBitrates by remember(initialBitrates) { mutableStateOf(initialBitrates) }
    var selectedCodecs by remember(initialCodecs) { mutableStateOf(initialCodecs) }

    fun toggleBitrate(v: String) {
        selectedBitrates = if (v == "Any") emptySet()
        else if (selectedBitrates.contains(v)) selectedBitrates - v
        else selectedBitrates + v
    }
    fun toggleCodec(v: String) {
        selectedCodecs = if (v == "Any") emptySet()
        else if (selectedCodecs.contains(v)) selectedCodecs - v
        else selectedCodecs + v
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RadioColors.Background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "FILTERS",
                fontFamily = Ndot57,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = RadioColors.TextPrimary,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "BITRATE — multi-select",
                fontFamily = Ndot57,
                fontSize = 14.sp,
                color = RadioColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Bitrate chips — multiselect
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val bitrateItems = listOf("Any", "64", "128", "192", "256", "320")
                items(bitrateItems.size) { idx ->
                    val item = bitrateItems[idx]
                    val isSelected = if (item == "Any") selectedBitrates.isEmpty() else selectedBitrates.contains(item)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .clip(RadioShapes.Chip)
                            .background(if (isSelected) RadioColors.Accent else androidx.compose.ui.graphics.Color.Transparent)
                            .border(1.dp, if (isSelected) RadioColors.Accent else RadioColors.Border, RadioShapes.Chip)
                            .clickable { toggleBitrate(item) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(item.uppercase(), color = if (isSelected) RadioColors.TextPrimary else RadioColors.TextSecondary, fontFamily = Ndot57, fontSize = 12.sp, letterSpacing = 1.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "CODEC — multi-select",
                fontFamily = Ndot57,
                fontSize = 14.sp,
                color = RadioColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val codecItems = listOf("Any", "MP3", "AAC", "OGG", "FLAC")
                items(codecItems.size) { idx ->
                    val item = codecItems[idx]
                    val isSelected = if (item == "Any") selectedCodecs.isEmpty() else selectedCodecs.contains(item)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .clip(RadioShapes.Chip)
                            .background(if (isSelected) RadioColors.Accent else androidx.compose.ui.graphics.Color.Transparent)
                            .border(1.dp, if (isSelected) RadioColors.Accent else RadioColors.Border, RadioShapes.Chip)
                            .clickable { toggleCodec(item) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(item.uppercase(), color = if (isSelected) RadioColors.TextPrimary else RadioColors.TextSecondary, fontFamily = Ndot57, fontSize = 12.sp, letterSpacing = 1.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedBitrates = emptySet()
                        selectedCodecs = emptySet()
                        onClear()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RadioShapes.Card,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RadioColors.TextPrimary
                    )
                ) {
                    Text("CLEAR", fontFamily = Ndot57)
                }
                
                Button(
                    onClick = {
                        onApply(selectedBitrates, selectedCodecs)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RadioShapes.Card,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadioColors.Accent,
                        contentColor = RadioColors.TextPrimary
                    )
                ) {
                    Text("APPLY", fontFamily = Ndot57)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
