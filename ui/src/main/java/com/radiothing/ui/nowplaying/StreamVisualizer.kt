package com.radiothing.ui.nowplaying

import android.Manifest
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.math.pow
import kotlinx.coroutines.delay

/**
 * Real stream-driven visuals. Oscilloscope (waveform) + dotted equalizer (FFT).
 * Both fall back to synthetic when session is 0 / permission denied / idle.
 */

// ── Dotted equalizer that looks like the reference image, but RED + WHITE ──
// Bars of dots, left tall → right short at rest, but when stream plays heights follow actual FFT.

@Composable
fun StreamDotEqualizer(
    audioSessionId: Int,
    isPlaying: Boolean,
    isBuffering: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 10,
    rowCount: Int = 26
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    LaunchedEffect(isPlaying) {
        while (true) {
            hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            delay(2000)
        }
    }

    var fftLevels by remember { mutableStateOf<FloatArray?>(null) }
    var vizRef by remember { mutableStateOf<Visualizer?>(null) }

    // Smoothing buffer — keeps motion from jittering
    var smoothed by remember { mutableStateOf(FloatArray(barCount) { 0.15f }) }

    DisposableEffect(audioSessionId, isPlaying, hasPermission) {
        vizRef?.release(); vizRef = null; fftLevels = null
        val can = hasPermission && isPlaying && !isBuffering && audioSessionId != 0 && audioSessionId != -1
        if (!can) { onDispose {} } else {
            var viz: Visualizer? = null
            try {
                viz = Visualizer(audioSessionId)
                val range = Visualizer.getCaptureSizeRange()
                viz.captureSize = range[1] // max for finest FFT
                viz.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer, w: ByteArray, sr: Int) {}
                    override fun onFftDataCapture(v: Visualizer, f: ByteArray, sr: Int) {
                        // f = FFT: interleaved real/imag
                        val n = f.size / 2
                        val mags = FloatArray(barCount)
                        // Log-spaced bands (like real EQs): bar 0 covers a NARROW low band instead of
                        // swallowing the whole bass region — this is what keeps it from pinning full
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
                                val re = f[i * 2].toInt()
                                val im = f[i * 2 + 1].toInt()
                                val mag = kotlin.math.hypot(re.toDouble(), im.toDouble()).toFloat()
                                if (mag > peak) peak = mag
                                c++
                                i++
                            }
                            mags[bar] = if (c > 0) peak else 0f
                        }
                        // Fixed-reference normalization (NOT per-frame max — max-normalization
                        // is what pinned bar 0: bass always won, everything else scaled to it).
                        // 96f ≈ loud bin magnitude for Visualizer FFT; tilt counters bass dominance.
                        val normalized = FloatArray(barCount) { idx ->
                            val tilt = 0.55 + 0.45 * (idx.toDouble() / (barCount - 1)) // bar0 ×0.55 → last ×1.0
                            val raw = (mags[idx] / 96f) * tilt
                            val v2 = raw.coerceIn(0.0, 1.0).pow(0.8)
                            (0.06 + v2 * 0.9).toFloat().coerceIn(0f, 1f)
                        }
                        fftLevels = normalized
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                viz.enabled = true
                vizRef = viz
            } catch (_: Exception) { viz?.release(); vizRef = null }
            onDispose {
                try { vizRef?.enabled = false; vizRef?.release() } catch (_: Exception) {}
                vizRef = null
            }
        }
    }

    // Smooth levels for rendering (lerp)
    val displayLevels = remember(fftLevels, isPlaying, isBuffering) {
        if (fftLevels != null && isPlaying && !isBuffering) {
            val incoming = fftLevels!!
            for (i in smoothed.indices) {
                val target = incoming[i.coerceIn(incoming.indices)]
                smoothed[i] = smoothed[i] + (target - smoothed[i]) * 0.35f
            }
            smoothed.copyOf()
        } else if (isBuffering) {
            // fake buffering sweep — gentle
            FloatArray(barCount) { 0.35f }
        } else if (!isPlaying) {
            FloatArray(barCount) { 0.08f }
        } else {
            // no real data yet but playing — fake dance that will be replaced by FFT on next capture
            FloatArray(barCount) { idx ->
                val base = 0.45f - idx * 0.032f
                (base + kotlin.math.sin(System.currentTimeMillis() / 280.0 + idx * 0.9).toFloat() * 0.12f).coerceIn(0.12f, 0.95f)
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
 * Real stream-driven oscilloscope. Uses Visualizer(audioSessionId) waveform when available,
 * falls back to synthetic sine when session is 0, permission denied, or player idle.
 * No fake data when stream is live — the trace follows the actual PCM.
 */
@Composable
fun StreamOscilloTrace(
    audioSessionId: Int,
    isPlaying: Boolean,
    isBuffering: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    // poll permission (user may grant while screen is open)
    LaunchedEffect(isPlaying) {
        while (true) {
            hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            delay(2000)
        }
    }

    var waveform by remember { mutableStateOf<ByteArray?>(null) }
    var visualizerRef by remember { mutableStateOf<Visualizer?>(null) }

    // Real capture — Dispose when session changes / stops
    DisposableEffect(audioSessionId, isPlaying, hasPermission) {
        // tear down previous
        visualizerRef?.release()
        visualizerRef = null
        waveform = null

        val canCapture = hasPermission && isPlaying && !isBuffering && audioSessionId != 0 && audioSessionId != -1
        if (!canCapture) {
            onDispose { }
        } else {
            var viz: Visualizer? = null
            try {
                viz = Visualizer(audioSessionId)
                // max capture size for smooth trace
                val range = Visualizer.getCaptureSizeRange()
                viz.captureSize = range[1]
                viz.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer, w: ByteArray, samplingRate: Int) {
                        // copy for compose
                        waveform = w.copyOf()
                    }
                    override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {}
                }, Visualizer.getMaxCaptureRate() / 2, true, false)
                viz.enabled = true
                visualizerRef = viz
            } catch (e: Exception) {
                viz?.release()
                visualizerRef = null
                // fallback will run via synthetic branch below
            }
            onDispose {
                try {
                    visualizerRef?.enabled = false
                    visualizerRef?.release()
                } catch (_: Exception) {}
                visualizerRef = null
            }
        }
    }

    // Render — real waveform when available, synthetic fallback otherwise (idle/buffering/permission denied/no session)
    if (waveform != null && isPlaying && !isBuffering && hasPermission) {
        // REAL trace
        Canvas(modifier = modifier) {
            val w = waveform ?: return@Canvas
            if (w.isEmpty()) return@Canvas
            val midY = size.height / 2
            // waveform bytes are unsigned 0..255, 128 = 0
            val stepX = size.width / (w.size.toFloat())
            val path = androidx.compose.ui.graphics.Path()
            var first = true
            for (i in w.indices) {
                val x = i * stepX
                val normalized = (w[i].toInt() and 0xFF) - 128
                val y = midY + (normalized / 128f) * (size.height * 0.42f)
                if (first) { path.moveTo(x, y); first = false } else path.lineTo(x, y)
            }
            val trace = Color(0xFFFF3344)
            drawPath(path, color = trace, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
            drawPath(path, color = trace.copy(alpha = 0.18f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 9f))
        }
    } else {
        // Synthetic fallback — flat line when idle, gentle fake when buffering, never used when real is live
        FakeOscilloTrace(isPlaying = isPlaying, isBuffering = isBuffering, modifier = modifier)
    }
}

@Composable
private fun FakeOscilloTrace(isPlaying: Boolean, isBuffering: Boolean, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "osc-fake")
    val phase by inf.animateFloat(0f, 6.28f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "phase")
    val amp by inf.animateFloat(0.6f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "amp")
    Canvas(modifier = modifier) {
        if (!isPlaying || isBuffering) {
            drawLine(color = Color(0xFF333333), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 2f)
            return@Canvas
        }
        val traceColor = Color(0xFFFF3344)
        val path = androidx.compose.ui.graphics.Path()
        val midY = size.height / 2
        val a = 22f * amp
        for (x in 0..size.width.toInt() step 3) {
            val xf = x.toFloat()
            val y = midY + a * kotlin.math.sin((xf * 0.04f + phase).toDouble()).toFloat() + (kotlin.math.sin((xf * 0.09f + phase * 1.3f).toDouble()).toFloat() * 6f)
            if (x == 0) path.moveTo(xf, y) else path.lineTo(xf, y)
        }
        drawPath(path, color = traceColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
        drawPath(path, color = traceColor.copy(alpha = 0.18f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 9f))
    }
}
