package com.radiothing.player.visualizer

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SpectrumTapProcessorTest {

    @Test
    fun `queueInput passes through audio data and sets output buffer readable`() {
        var binsReceived: FloatArray? = null
        val processor = SpectrumTapProcessor { bins ->
            binsReceived = bins
        }

        val format = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
        val outFormat = processor.configure(format)
        assertEquals(format, outFormat)
        processor.flush()

        val sampleCount = 1024 * 2
        val input = ByteBuffer.allocateDirect(sampleCount * 2).order(ByteOrder.nativeOrder())
        for (i in 0 until sampleCount) {
            input.putShort((i % 1000).toShort())
        }
        input.flip()

        processor.queueInput(input)
        val output = processor.output

        assertEquals(0, input.remaining())
        assertEquals(sampleCount * 2, output.remaining())
        assertEquals(512, binsReceived?.size)
    }

    @Test
    fun `each FFT window publishes an independent snapshot`() {
        val emitted = mutableListOf<FloatArray>()
        val processor = SpectrumTapProcessor { bins -> emitted += bins }

        val format = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
        processor.configure(format)
        processor.flush()

        val frames = 1024 * 2
        val input = ByteBuffer.allocateDirect(frames * 2 * 2).order(ByteOrder.nativeOrder())
        repeat(frames) { frame ->
            val sample = if (frame < 1024) 1 else 2
            input.putShort(sample.toShort())
            input.putShort(sample.toShort())
        }
        input.flip()

        processor.queueInput(input)

        assertEquals(2, emitted.size)
        assertNotSame(emitted[0], emitted[1])
    }

    @Test
    fun `queueInput with empty buffer does not crash`() {
        val processor = SpectrumTapProcessor {}
        val format = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
        processor.configure(format)
        processor.flush()

        val empty = ByteBuffer.allocateDirect(0)
        processor.queueInput(empty)
        val output = processor.output
        assertEquals(0, output.remaining())
    }
}
