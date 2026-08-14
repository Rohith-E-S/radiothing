package com.radiothing.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.RadioColors
import com.radiothing.ui.theme.RadioShapes

@Composable
fun StationListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        repeat(6) {
            SkeletonItem()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SkeletonItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RadioColors.Surface, RadioShapes.Card)
            .border(1.dp, RadioColors.Border, RadioShapes.Card)
            .padding(12.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favicon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(RadioColors.Border)
        ) {
            Text(
                "▓",
                modifier = Modifier.align(Alignment.Center),
                color = RadioColors.TextTertiary,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // Title placeholder
            Text(
                text = "████████████",
                color = RadioColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Subtitle placeholder
            Text(
                text = "▒▒▒▒ ░░░░░░░",
                color = RadioColors.TextTertiary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}
