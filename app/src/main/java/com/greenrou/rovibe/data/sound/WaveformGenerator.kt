package com.greenrou.rovibe.data.sound

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object WaveformGenerator {
    const val SAMPLE_RATE = 44100

    fun silence(durationMs: Long): ShortArray = ShortArray(sampleCount(durationMs))

    fun sineWave(frequencyHz: Float, durationMs: Long, amplitude: Float = 1f): ShortArray {
        val count = sampleCount(durationMs)
        return ShortArray(count) { i ->
            val angle = 2.0 * PI * i * frequencyHz / SAMPLE_RATE
            val envelope = decayEnvelope(i, count)
            (sin(angle) * amplitude * envelope * Short.MAX_VALUE).toInt().toShort()
        }
    }

    fun squareWave(frequencyHz: Float, durationMs: Long, amplitude: Float = 1f): ShortArray {
        val count = sampleCount(durationMs)
        val period = SAMPLE_RATE / frequencyHz
        return ShortArray(count) { i ->
            val phase = (i % period) / period
            val envelope = decayEnvelope(i, count)
            val value = if (phase < 0.5f) 1f else -1f
            (value * amplitude * envelope * Short.MAX_VALUE).toInt().toShort()
        }
    }

    fun noise(durationMs: Long, amplitude: Float = 1f): ShortArray {
        val count = sampleCount(durationMs)
        return ShortArray(count) { i ->
            val envelope = decayEnvelope(i, count)
            ((Random.nextFloat() * 2f - 1f) * amplitude * envelope * Short.MAX_VALUE).toInt().toShort()
        }
    }

    fun mix(tracks: List<ShortArray>): ShortArray {
        val length = tracks.maxOfOrNull { it.size } ?: return ShortArray(0)
        val sums = IntArray(length)
        for (track in tracks) {
            for (i in track.indices) {
                sums[i] += track[i]
            }
        }
        return ShortArray(length) { i ->
            sums[i].coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun concat(segments: List<ShortArray>): ShortArray {
        val result = ShortArray(segments.sumOf { it.size })
        var offset = 0
        for (segment in segments) {
            segment.copyInto(result, offset)
            offset += segment.size
        }
        return result
    }

    private fun decayEnvelope(index: Int, total: Int): Float =
        if (total <= 1) 1f else 1f - index.toFloat() / total

    private fun sampleCount(durationMs: Long): Int =
        (SAMPLE_RATE * durationMs / 1000).toInt()
}
