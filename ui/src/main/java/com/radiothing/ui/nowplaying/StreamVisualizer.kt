package com.radiothing.ui.nowplaying

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.pow

/**
 * Real stream-driven visuals. Dotted equalizer (FFT) driven by the player's
 * PCM tap (PlayerManager.spectrumBins) — no RECORD_AUDIO permission involved.
 * Falls back to synthetic when no spectrum data arrives (audio offload to
 * DSP bypasses the PCM path) or while idle.
 */

// ── Dotted equalizer that looks like the reference image, but RED + WHITE ──
// Bars of dots, left tall → right short at rest, but when stream plays heights follow actual FFT.

@Composable
fun StreamDotEqualizer(
    spectrumBins: StateFlow<FloatArray?>,
    isPlaying: Boolean,
    isBuffering: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 10,
    rowCount: Int = 26
) {
    val incomingBins by spectrumBins.collectAsStateWithLifecycle()
    val fallbackTransition = rememberInfiniteTransition(label = "streamVisualizerFallback")
    val fallbackPhase by fallbackTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = LinearEasing)
        ),
        label = "fallbackPhase"
    )

    var fftLevels by remember { mutableStateOf<FloatArray?>(null) }

    // Collapse 512 spectrum bins into barCount log-spaced bands — same mapping
    // (and the same fixed-reference normalization) the Visualizer path used,
    // since SpectrumTapProcessor emits Visualizer-compatible magnitudes.
    val banded = remember(incomingBins, barCount) {
        incomingBins?.let { bins -> bandSpectrum(bins, barCount) }
    }
    LaunchedEffect(banded) {
        // Clear the previous station's spectrum when the PCM path is absent
        // (for example, an offloaded stream) so the fallback can take over.
        fftLevels = banded
    }

    // Smoothing buffer — keeps motion from jittering
    var smoothed by remember { mutableStateOf(FloatArray(barCount) { 0.15f }) }

    // Smooth levels for rendering (lerp)
    val displayLevels = remember(fftLevels, isPlaying, isBuffering, fallbackPhase) {
        if (fftLevels != null && isPlaying && !isBuffering) {
            val incoming = fftLevels!!
            for (i in smoothed.indices) {
                val target = incoming[i.coerceIn(incoming.indices)]
                smoothed[i] = smoothed[i] + (target - smoothed[i]) * 0.35f
            }
            smoothed.copyOf()
        } else if (isBuffering) {
            // Animated fallback while the stream is buffering or offloaded.
            FloatArray(barCount) { idx ->
                (0.35f + kotlin.math.sin(fallbackPhase + idx * 0.58f) * 0.18f)
                    .coerceIn(0.12f, 0.58f)
            }
        } else if (!isPlaying) {
            FloatArray(barCount) { 0.08f }
        } else {
            // No real data yet but playing — fake dance that will be replaced
            // by FFT on the next capture.
            FloatArray(barCount) { idx ->
                val base = 0.45f - idx * 0.032f
                (base + kotlin.math.sin(fallbackPhase + idx * 0.9f) * 0.12f)
                    .coerceIn(0.12f, 0.95f)
            }
        }
    }

    // Render dotted bars — RED + WHITE only, tight like reference (tiny dots, 1-2px gaps)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gapX = w * 0.035f // tighter inter-bar gap like reference
        val totalGap = gapX * (barCount - 1)
        val colW = (w - totalGap) / barCount
        val dotsPerRow = 8
        val dotRadius = (colW * 0.11f).coerceIn(1.6f, 2.4f)
        val dotGapY = h / rowCount * 0.62f
        val dotGapX = colW / dotsPerRow

        for (bar in 0 until barCount) {
            val lvl = displayLevels[bar.coerceIn(displayLevels.indices)].coerceIn(0f, 1f)
            val activeRows = (lvl * rowCount).toInt().coerceIn(0, rowCount)
            val x0 = bar * (colW + gapX)

            for (row in 0 until rowCount) {
                val y = h - row * dotGapY - dotGapY * 0.38f
                if (y < 0 || y > h) continue
                val isActive = row < activeRows
                for (col in 0 until dotsPerRow) {
                    val x = x0 + col * dotGapX + dotGapX * 0.5f
                    if (x > x0 + colW - dotRadius * 0.5f) continue
                    val frac = row / (rowCount - 1f)
                    val color = when {
                        !isActive -> Color(0xFF1E1E1E)
                        frac > 0.88f -> Color.White
                        frac > 0.65f -> Color(0xFFFF6B6B)
                        frac > 0.35f -> Color(0xFFFF3344)
                        else -> Color(0xFFFF3344).copy(alpha = 0.92f)
                    }
                    drawCircle(color = color, radius = dotRadius, center = Offset(x, y))
                }
            }
        }
    }
}

/**
 * Log-spaced band peaks over the raw spectrum (like real EQs): bar 0 covers a
 * NARROW low band instead of swallowing the whole bass region — this is what
 * keeps it from pinning full.
 *
 * Fixed-reference normalization (NOT per-frame max — max-normalization is
 * what pinned bar 0: bass always won, everything else scaled to it).
 * 96f ≈ loud bin magnitude for Visualizer FFT; tilt counters bass dominance.
 */
private fun bandSpectrum(bins: FloatArray, barCount: Int): FloatArray {
    val n = bins.size
    val mags = FloatArray(barCount)
    val minBin = 2 // skip DC + leakage
    val maxBin = n - 1
    for (bar in 0 until barCount) {
        val lo = minBin + ((maxBin - minBin).toDouble() * (bar.toDouble() / barCount).pow(1.8)).toInt()
        val hi = minBin + ((maxBin - minBin).toDouble() * ((bar + 1).toDouble() / barCount).pow(1.8)).toInt()
        val end = hi.coerceAtMost(maxBin)
        var peak = 0f
        var c = 0
        var i = lo
        while (i < end) {
            if (bins[i] > peak) peak = bins[i]
            c++
            i++
        }
        mags[bar] = if (c > 0) peak else 0f
    }
    return FloatArray(barCount) { idx ->
        val tilt = 0.55 + 0.45 * (idx.toDouble() / (barCount - 1)) // bar0 ×0.55 → last ×1.0
        val raw = (mags[idx] / 96f) * tilt
        val v2 = raw.coerceIn(0.0, 1.0).pow(0.8)
        (0.06 + v2 * 0.9).toFloat().coerceIn(0f, 1f)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L, name = "Stream equalizer idle")
@Composable
private fun StreamDotEqualizerIdlePreview() {
    StreamDotEqualizer(
        spectrumBins = MutableStateFlow(null),
        isPlaying = false,
        isBuffering = false,
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050507L, name = "Stream equalizer playing")
@Composable
private fun StreamDotEqualizerPlayingPreview() {
    StreamDotEqualizer(
        spectrumBins = MutableStateFlow(null),
        isPlaying = true,
        isBuffering = false,
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)
    )
}
