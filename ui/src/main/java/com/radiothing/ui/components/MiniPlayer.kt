package com.radiothing.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import com.radiothing.ui.theme.Ndot57
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiothing.domain.model.PlayerState
import com.radiothing.ui.theme.BrightRed
import com.radiothing.ui.theme.GridLine
import com.radiothing.ui.theme.Ink
import com.radiothing.ui.theme.Panel
import com.radiothing.ui.theme.TextWhite70

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onPlayPauseClick: () -> Unit,
    onExpandClick: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = playerState.currentStation != null && (playerState.isPlaying || playerState.isBuffering),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Mini player, tap to expand, swipe up" }
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            // Read inside the gesture below without restarting pointerInput
            val currentOnExpand by rememberUpdatedState(onExpandClick)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Panel)
                    .border(1.dp, if (playerState.isPlaying) BrightRed.copy(alpha = 0.45f) else GridLine.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .pointerInput(Unit) {
                        // Accumulate the drag across the whole gesture and expand
                        // once on release: per-event deltas (<-20) fired on every
                        // frame of a fast fling (multiple navigations per gesture)
                        // and never on a slow deliberate drag.
                        var accumulatedDrag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { accumulatedDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDrag += dragAmount
                            },
                            onDragEnd = {
                                if (accumulatedDrag < -40) currentOnExpand()
                            }
                        )
                    }
                    .clickable(onClick = onExpandClick)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lab indicator strip
                if (playerState.isPlaying) {
                    val infiniteTransition = rememberInfiniteTransition(label = "mini")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                        label = "miniPulse"
                    )
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(BrightRed.copy(alpha = alpha))
                    )
                    Spacer(Modifier.width(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(GridLine)
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerState.currentStation?.name?.uppercase() ?: "",
                        color = Color.White,
                        fontFamily = Ndot57,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(Modifier.height(2.dp))
                    if (playerState.isBuffering) {
                        val infiniteTransition = rememberInfiniteTransition(label = "buf")
                        val dotCount by infiniteTransition.animateValue(0, 3, Int.VectorConverter, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "buffering")
                        Text(
                            text = "TUNING${".".repeat(dotCount)}",
                            color = BrightRed,
                            fontFamily = Ndot57,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    } else {
                        val queueInfo = if (playerState.queue.size > 1) " • ${playerState.queueIndex + 1}/${playerState.queue.size}" else ""
                        val meta = "${playerState.currentStation?.bitrate?.takeIf { it > 0 }?.let { "${it}K" } ?: "LIVE"}${playerState.currentStation?.codec?.takeIf { it.isNotEmpty() }?.let { " • ${it.uppercase()}" } ?: ""}$queueInfo"
                        Text(
                            text = meta,
                            color = TextWhite70,
                            fontFamily = Ndot57,
                            fontSize = 10.sp,
                            maxLines = 1,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))
                // Prev/next only render when a queue exists — otherwise they're
                // visible but disabled and look broken for single-station playback.
                if (playerState.queue.size > 1) {
                    // Prev — compact skip-back, mirrors the Next enclosure button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Transparent)
                            .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                            .clickable(enabled = playerState.queue.size > 1, onClick = onPrevious),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous station",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    // Next — compact skip, keeps bad streams one tap away
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Transparent)
                            .border(1.dp, GridLine, RoundedCornerShape(100.dp))
                            .clickable(enabled = playerState.queue.size > 1, onClick = onNext),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next station",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                // Play/Pause — enclosure button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrightRed)
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
