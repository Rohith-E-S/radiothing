package com.radiothing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.ui.theme.RadioColors

@Composable
fun VolumeBar(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentVolume by remember { mutableFloatStateOf(volume.coerceIn(0f, 1f)) }
    val segments = 10
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    val width = size.width.toFloat()
                    val diff = dragAmount / width
                    currentVolume = (currentVolume + diff).coerceIn(0f, 1f)
                    onVolumeChange(currentVolume)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val filledCount = (currentVolume * segments).toInt()
        val emptyCount = segments - filledCount
        
        val filledChars = "■".repeat(filledCount)
        val emptyChars = "□".repeat(emptyCount)
        
        Text(
            text = "[$filledChars$emptyChars]",
            fontFamily = FontFamily.Monospace,
            color = RadioColors.TextPrimary,
            fontSize = 18.sp,
            letterSpacing = 2.sp
        )
    }
}
