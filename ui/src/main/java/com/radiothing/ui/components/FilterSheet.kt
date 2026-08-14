package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.RadioColors
import com.radiothing.ui.theme.RadioShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    onApply: (country: String?, bitrate: String?, codec: String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var selectedBitrate by remember { mutableStateOf<String?>("Any") }
    var selectedCodec by remember { mutableStateOf<String?>("Any") }

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
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = RadioColors.TextPrimary,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "BITRATE",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = RadioColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterChips(
                items = listOf("Any", "64", "128", "192", "256", "320"),
                selectedItem = selectedBitrate,
                onItemClick = { selectedBitrate = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "CODEC",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = RadioColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterChips(
                items = listOf("Any", "MP3", "AAC", "OGG", "FLAC"),
                selectedItem = selectedCodec,
                onItemClick = { selectedCodec = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedCountry = null
                        selectedBitrate = "Any"
                        selectedCodec = "Any"
                        onClear()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RadioShapes.Card,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RadioColors.TextPrimary
                    )
                ) {
                    Text("CLEAR", fontFamily = FontFamily.Monospace)
                }
                
                Button(
                    onClick = {
                        onApply(selectedCountry, selectedBitrate, selectedCodec)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RadioShapes.Card,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadioColors.Accent,
                        contentColor = RadioColors.TextPrimary
                    )
                ) {
                    Text("APPLY", fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
