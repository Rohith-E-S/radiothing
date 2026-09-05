package com.radiothing.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
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
    // Subtle pulse — animates alpha between 0.4 and 0.7 every 1.2s. Lighter than
    // a sweeping shimmer (which on a 6+ item list costs 6 InfiniteTransitions and
    // visible per-frame repaint), but clearly alive instead of dead-static.
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RadioColors.Surface, RadioShapes.Card)
            .border(1.dp, RadioColors.Border, RadioShapes.Card)
            .padding(14.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(RadioColors.Border, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
        ) {
            Text(
                "∿",
                modifier = Modifier.align(Alignment.Center),
                color = RadioColors.TextTertiary,
                fontFamily = Ndot57,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title placeholder
            Text(
                text = "████████████",
                color = RadioColors.TextSecondary,
                fontFamily = Ndot57,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Subtitle placeholder
            Text(
                text = "▒▒▒▒ ░░░░░░░",
                color = RadioColors.TextTertiary,
                fontFamily = Ndot57,
                fontSize = 12.sp
            )
        }
    }
}
