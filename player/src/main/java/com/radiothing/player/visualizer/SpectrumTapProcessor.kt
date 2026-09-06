package com.radiothing.player.visualizer

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.UnhandledAudioFormatException
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot

/**
 * PCM tap that turns the audible output into a 512-bin magnitude spectrum
 * (FFT over the last 1024 frames), published via [onBins].
 *
 * Replaces android.media.audiofx.Visualizer, which required the mic-flagged
 * RECORD_AUDIO permission even though it only ever captured the app's own
 * playback. Tapping our own pipeline through the audio sink needs no
 * permission at all — and removes "Microphone" from the Play listing.
 *
 * Runs on the playback (audio sink) thread. Bin magnitudes are scaled to
 * roughly the convention Visualizer's FFT bytes used (a loud bin ≈ 96), so
 * the UI's normalization tuning carries over unchanged. Passes the audio
 * through untouched — this is analysis only.
 *
 * Bypassed when audio is offloaded to the DSP (no PCM flows here) — the UI
 * falls back to its synthetic animation in that case, same as when the
 * Visualizer failed to attach.
 */
class SpectrumTapProcessor(private val onBins: (FloatArray) -> Unit) : BaseAudioProcessor() {

    private companion object {
        const val FRAME_SIZE = 1024
        const val BIN_COUNT = FRAME_SIZE / 2
    }

    private var channelCount = 0

    // Mono accumulation of the last FRAME_SIZE samples, byte-scale (±128 like
    // Visualizer's 8-bit capture), interleaved channels mixed down.
    private val mono = FloatArray(FRAME_SIZE)
    private var filled = 0

    // Bytes of an interleaved frame split across queueInput calls — kept so
    // channel alignment survives buffer boundaries.
    private val pending = ByteArray(8)
    private var pendingSize = 0

    private val real = DoubleArray(FRAME_SIZE)
    private val imag = DoubleArray(FRAME_SIZE)
    private val window = FloatArray(FRAME_SIZE)
    private val bins = FloatArray(BIN_COUNT)

    init {
        for (i in 0 until FRAME_SIZE) {
            window[i] = (0.5 * (1.0 - cos(2.0 * PI * i / FRAME_SIZE))).toFloat() // Hann
        }
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT &&
            inputAudioFormat.encoding != C.ENCODING_PCM_FLOAT
        ) {
            throw UnhandledAudioFormatException(inputAudioFormat)
        }
        channelCount = inputAudioFormat.channelCount
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val frameBytes = channelCount * (if (inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT) 4 else 2)
        val dup = inputBuffer.duplicate() // read via duplicate — analysis never disturbs the stream

        if (pendingSize in 1 until frameBytes) {
            while (pendingSize < frameBytes && dup.hasRemaining()) pending[pendingSize++] = dup.get()
            if (pendingSize == frameBytes) {
                consumeFrame(ByteBuffer.wrap(pending, 0, frameBytes).order(dup.order()), frameBytes)
                pendingSize = 0
            }
        }
        while (dup.remaining() >= frameBytes) consumeFrame(dup, frameBytes)
        if (dup.hasRemaining()) {
            // Frame split across buffer boundaries — carry the bytes over
            while (dup.hasRemaining()) pending[pendingSize++] = dup.get()
        }
        // Pass the untouched audio through to the next stage in the chain
        val out = replaceOutputBuffer(inputBuffer.remaining())
        out.put(inputBuffer)
    }

    private fun consumeFrame(frame: ByteBuffer, frameBytes: Int) {
        var sum = 0f
        if (frameBytes == channelCount * 4) {
            repeat(channelCount) { sum += frame.float }
        } else {
            repeat(channelCount) { sum += frame.short.toInt() }
        }
        val sample = sum / channelCount
        val scaled = if (frameBytes == channelCount * 4) sample * 128f else sample / 256f
        val idx = filled
        mono[idx] = scaled * window[idx]
        filled = idx + 1
        if (filled == FRAME_SIZE) {
            publishSpectrum()
            filled = 0
        }
    }

    private fun publishSpectrum() {
        System.arraycopy(mono, 0, real, 0, FRAME_SIZE)
        java.util.Arrays.fill(imag, 0.0)
        fft(real, imag)
        // |X_k|·(4/N): N/2 for a sinusoid's DFT bin, ×2 Hann coherent gain
        // compensation → magnitude lands back in byte-scale units (loud ≈ 96+).
        val scale = 4.0 / FRAME_SIZE
        for (k in 0 until BIN_COUNT) {
            bins[k] = (hypot(real[k], imag[k]) * scale).toFloat().coerceIn(0f, 127f)
        }
        onBins(bins)
    }

    /** In-place iterative radix-2 FFT (FRAME_SIZE must be a power of two). */
    private fun fft(re: DoubleArray, im: DoubleArray) {
        val n = re.size
        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
                val ti = im[i]; im[i] = im[j]; im[j] = ti
            }
        }
        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wr = cos(ang)
            val wi = kotlin.math.sin(ang)
            var start = 0
            while (start < n) {
                var cRe = 1.0
                var cIm = 0.0
                var k = start
                val mid = start + len / 2
                while (k < mid) {
                    val ur = re[k]
                    val ui = im[k]
                    val vr = re[k + len / 2] * cRe - im[k + len / 2] * cIm
                    val vi = re[k + len / 2] * cIm + im[k + len / 2] * cRe
                    re[k] = ur + vr
                    im[k] = ui + vi
                    re[k + len / 2] = ur - vr
                    im[k + len / 2] = ui - vi
                    val nextRe = cRe * wr - cIm * wi
                    cIm = cRe * wi + cIm * wr
                    cRe = nextRe
                    k++
                }
                start += len
            }
            len = len shl 1
        }
    }
}
