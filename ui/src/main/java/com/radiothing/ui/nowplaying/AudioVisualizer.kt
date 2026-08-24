package com.radiothing.ui.nowplaying

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NothingRed = Color(0xFFFF2D2D)
private val Bg = Color(0xFF0A0A0A)
private val BgInner = Color(0xFF0F0F0F)
private val BorderGray = Color(0xFF1E1E1E)
private val InactiveDot = Color(0xFF181818)
private val InactiveDotBorder = Color(0xFF242424)

@Composable
fun DotMatrixVisualizer(
    isPlaying: Boolean,
    isBuffering: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 28
    val rowCount = 9
    val infinite = rememberInfiniteTransition(label = "viz")

    val levels = remember(isPlaying, isBuffering) { (0 until barCount).map { it } }

    val animated = levels.map { idx ->
        if (isBuffering) {
            val phase = infinite.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 850 + (idx % 4) * 110, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "buf$idx"
            )
            phase
        } else if (isPlaying) {
            infinite.animateFloat(
                initialValue = 0.32f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 260 + (idx * 41 % 240),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$idx"
            )
        } else {
            infinite.animateFloat(
                initialValue = 0.10f,
                targetValue = 0.22f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "idle$idx"
            )
        }
    }

    val borderPulse by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Bg)
            .border(
                width = 1.dp,
                color = if (isPlaying) NothingRed.copy(alpha = 0.28f + borderPulse * 0.22f) else BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        // subtle inner gradient band
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(BgInner)
        )
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val w = size.width
            val h = size.height
            val colW = w / barCount
            // tighter dot matrix
            val dotRadius = (colW * 0.32f).coerceIn(2.2f, 4.2f)
            val dotGapY = h / rowCount

            for (barIdx in 0 until barCount) {
                val animVal = animated[barIdx].value
                val levelNormalized = if (isBuffering) {
                    val sweep = (kotlin.math.sin((animVal * 2 * Math.PI) + barIdx * 0.58) * 0.5 + 0.5).toFloat()
                    (sweep * 0.88f + 0.12f)
                } else if (isPlaying) {
                    // boost low end so visualizer never looks empty; add per-bar shape
                    val shape = 0.62f + (barIdx % 4) * 0.08f + (if (barIdx % 7 == 0) 0.12f else 0f)
                    (animVal * shape).coerceIn(0.22f, 1f)
                } else {
                    animVal * 0.9f + 0.08f
                }

                val activeRows = (levelNormalized * rowCount).toInt().coerceIn(1, rowCount)
                val x = colW * barIdx + colW / 2

                for (row in 0 until rowCount) {
                    val y = h - (row * dotGapY) - dotGapY / 2
                    val isActive = row < activeRows
                    val frac = row / (rowCount - 1f)
                    val color = when {
                        !isActive -> InactiveDot
                        isBuffering -> NothingRed.copy(alpha = 0.88f)
                        frac > 0.78f -> NothingRed // peak white-red feel
                        frac > 0.45f -> NothingRed.copy(alpha = 0.95f)
                        else -> NothingRed.copy(alpha = 0.78f + frac * 0.15f)
                    }
                    if (!isActive) {
                        drawCircle(
                            color = InactiveDotBorder,
                            radius = dotRadius * 0.85f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = InactiveDot,
                            radius = dotRadius * 0.58f,
                            center = Offset(x, y)
                        )
                    } else {
                        // tip glow for top active dot
                        if (isPlaying && row == activeRows - 1) {
                            drawCircle(
                                color = color.copy(alpha = 0.22f),
                                radius = dotRadius * 2.1f,
                                center = Offset(x, y)
                            )
                        }
                        drawCircle(
                            color = color,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        // inner highlight
                        if (row >= rowCount - 2) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.22f),
                                radius = dotRadius * 0.35f,
                                center = Offset(x - dotRadius * 0.25f, y - dotRadius * 0.25f)
                            )
                        }
                    }
                }
            }
        }

        if (isPlaying && !isBuffering) {
            val pulse by infinite.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
                label = "hair"
            )
            Box(
                Modifier
                    .align(androidx.compose.ui.Alignment.TopCenter)
                    .fillMaxWidth(0.38f)
                    .height(1.dp)
                    .background(NothingRed.copy(alpha = 0.45f * pulse))
            )
        }
    }
}

@Composable
fun MiniWaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "miniWave")
    val bars = 5
    val anims = (0 until bars).map { i ->
        infinite.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(320 + i * 70, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "mw$i"
        )
    }
    Canvas(modifier = modifier.height(14.dp).width(28.dp)) {
        val barW = size.width / (bars * 1.6f)
        val gap = barW * 0.6f
        val totalW = bars * barW + (bars - 1) * gap
        val startX = (size.width - totalW) / 2
        for (i in 0 until bars) {
            val lvl = if (isPlaying) anims[i].value else 0.18f
            val h = size.height * lvl
            val x = startX + i * (barW + gap)
            val y = (size.height - h) / 2
            drawRoundRect(
                color = if (isPlaying) NothingRed else Color(0xFF333333),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
            )
        }
    }
}
