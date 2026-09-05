package com.radiothing.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Decorative chrome drawn with Canvas instead of block/sine text glyphs —
 * characters like █ ▓ ▒ ░ ∿ are absent from Ndot57, so text versions silently
 * render in the system fallback font with mismatched width/baseline.
 */
@Composable
fun SineWaveGlyph(
    color: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val amplitude = h * 0.32f
        val path = Path().apply {
            moveTo(0f, midY)
            // one full sine period across the width
            cubicTo(w * 0.17f, midY - amplitude, w * 0.33f, midY - amplitude, w * 0.5f, midY)
            cubicTo(w * 0.67f, midY + amplitude, w * 0.83f, midY + amplitude, w, midY)
        }
        drawPath(path, color = color, style = Stroke(width = stroke.toPx()))
    }
}

/**
 * Three small squares fading left-to-right — the drawn equivalent of "▓▒░".
 */
@Composable
fun BlockFade(
    color: Color,
    modifier: Modifier = Modifier,
    blockSize: Dp = 8.dp,
    gap: Dp = 2.dp
) {
    Row(modifier = modifier) {
        listOf(1f, 0.6f, 0.3f).forEachIndexed { index, alpha ->
            Box(
                modifier = Modifier
                    .size(blockSize)
                    .alpha(alpha)
                    .background(color)
            )
            if (index < 2) Spacer(Modifier.width(gap))
        }
    }
}
