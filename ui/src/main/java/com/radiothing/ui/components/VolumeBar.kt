package com.radiothing.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.RadioColors

/**
 * Stateless block-style volume indicator. Drag horizontally to change
 * the value. The component stays a controlled view of [volume] — when
 * the parent updates [volume] (e.g., from MiniPlayer), the bar resyncs.
 */
@Composable
fun VolumeBar(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local offset since the last parent update — small drag tweaks feel
    // instant without a feedback round-trip on every pixel. Resets when
    // the parent value changes (so external updates aren't clobbered).
    val segments = 10

    // Latest values via rememberUpdatedState: the pointerInput below must be
    // keyed on Unit — keying on volume restarts gesture detection on every
    // drag frame, killing the in-progress drag after a single step.
    val currentVolume by rememberUpdatedState(volume)
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    val width = size.width.toFloat()
                    val diff = dragAmount / width
                    currentOnVolumeChange((currentVolume + diff).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val filledCount = (volume.coerceIn(0f, 1f) * segments).toInt()
        val emptyCount = segments - filledCount

        val filledChars = "■".repeat(filledCount)
        val emptyChars = "□".repeat(emptyCount)

        Text(
            text = "[$filledChars$emptyChars]",
            fontFamily = Ndot57,
            color = RadioColors.TextPrimary,
            fontSize = 18.sp,
            letterSpacing = 2.sp
        )
    }
}
