package com.radiothing.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.radiothing.ui.theme.RadioThingTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite35
import com.radiothing.ui.theme.TextWhite70

enum class EmptyStateType {
    NO_RESULTS, NO_FAVORITES, NO_PLAYLISTS, NO_HISTORY
}

@Composable
fun EmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier
) {
    val (title, subtitle, hint) = when (type) {
        EmptyStateType.NO_RESULTS -> Triple(
            "NO SIGNAL",
            "No stations match your query",
            "TRY ANOTHER FREQUENCY"
        )
        EmptyStateType.NO_FAVORITES -> Triple(
            "NO FAVORITES YET",
            "Save stations you love",
            "BROWSE → TAP HEART"
        )
        EmptyStateType.NO_PLAYLISTS -> Triple(
            "NO PLAYLISTS YET",
            "Group stations into collections",
            "CREATE YOUR FIRST LIST"
        )
        EmptyStateType.NO_HISTORY -> Triple(
            "LOG EMPTY",
            "Your listening history lives here",
            "HIT PLAY TO BEGIN"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Instrument plate
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Panel)
                .border(1.dp, GridLine, RoundedCornerShape(16.dp))
                .padding(horizontal = 28.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D0D0F))
                        .border(1.dp, GridLine, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    com.radiothing.ui.components.SineWaveGlyph(color = BrightRed, modifier = Modifier.size(28.dp, 14.dp), stroke = 3.dp)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = Ndot57,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    color = TextWhite70,
                    fontFamily = Ndot57,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = hint,
                    color = TextWhite35,
                    fontFamily = Ndot57,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L)
@Composable
private fun EmptyStatePreviews() {
    RadioThingTheme {
        Column {
            EmptyState(type = EmptyStateType.NO_RESULTS)
            EmptyState(type = EmptyStateType.NO_FAVORITES)
        }
    }
}
