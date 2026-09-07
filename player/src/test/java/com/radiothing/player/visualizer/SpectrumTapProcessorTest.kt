package com.radiothing.player.visualizer

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import org.junit.Assert.assertEquals
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
