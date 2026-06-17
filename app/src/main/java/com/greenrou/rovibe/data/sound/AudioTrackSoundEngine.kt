package com.greenrou.rovibe.data.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val BIT_FREQUENCY_HZ = 1000f
private const val BIT_HIT_MS = 60L
private const val BASS_FREQUENCY_HZ = 60f
private const val BASS_HIT_MS = 150L
private const val HIHAT_HIT_MS = 30L
private const val SNARE_HIT_MS = 100L
private const val CLAP_HIT_MS = 70L
private const val TOM_FREQUENCY_HZ = 110f
private const val TOM_HIT_MS = 180L
private const val CRASH_HIT_MS = 500L
private const val PIANO_HIT_MS = 200L
private const val DEFAULT_TEMPO_BPM = 120
private const val AMPLITUDE_WINDOW_SAMPLES = 256
private const val AMPLITUDE_POLL_MS = 16L
private const val WRITE_CHUNK_BYTES = 4096
private const val FFT_SIZE = 2048
private const val BAND_COUNT = 24
private const val BAND_FREQ_MIN = 20f
private const val BAND_FREQ_MAX = 20000f
private const val BAND_ATTACK = 0.6f
private const val BAND_RELEASE = 0.12f
private const val BAND_GAIN = 2f

class AudioTrackSoundEngine : SoundEngine {

    private var audioTrack: AudioTrack? = null
    private var playbackGeneration = 0
    private var renderedSamples: ShortArray = ShortArray(0)
    private var renderedBytes: ByteArray = ByteArray(0)
    @Volatile private var writerOffset: Int = 0
    private var amplitudeJob: Job? = null
    private var writerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _spectrumBands = MutableStateFlow(FloatArray(BAND_COUNT))
    override val spectrumBands: StateFlow<FloatArray> = _spectrumBands.asStateFlow()
    private val smoothedBands = FloatArray(BAND_COUNT)

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0L)
    override val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    override fun play(commands: List<SoundCommand>) {
        stop()
        playbackGeneration++
        val generation = playbackGeneration

        val state = EngineState()
        val tracks = commands.map { render(it, state) }
        val offsets = startOffsets(commands, tracks)
        val padded = tracks.mapIndexed { index, track -> pad(track, offsets[index]) }
        val samples = WaveformGenerator.mix(padded)
        if (samples.isEmpty()) return
        renderedSamples = samples
        _totalDurationMs.value = samples.size * 1000L / WaveformGenerator.SAMPLE_RATE
        _currentPositionMs.value = 0L

        val bytes = toPcm16(samples)
        val bufferSizeBytes = AudioTrack.getMinBufferSize(
            WaveformGenerator.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ) * 2
        val track = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(WaveformGenerator.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bufferSizeBytes,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        audioTrack = track
        renderedBytes = bytes
        writerOffset = 0
        track.setNotificationMarkerPosition(samples.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack) {
                if (generation == playbackGeneration) {
                    _playbackState.value = PlaybackState.STOPPED
                }
            }

            override fun onPeriodicNotification(track: AudioTrack) = Unit
        })
        track.play()
        _playbackState.value = PlaybackState.PLAYING
        startAmplitudePolling(generation)
        startWriter(track, bytes, generation)
    }

    override fun pause() {
        val track = audioTrack ?: return
        if (_playbackState.value != PlaybackState.PLAYING) return
        track.pause()
        _playbackState.value = PlaybackState.PAUSED
        _amplitude.value = 0f
        _spectrumBands.value = FloatArray(BAND_COUNT)
        smoothedBands.fill(0f)
    }

    override fun resume() {
        val track = audioTrack ?: return
        if (_playbackState.value != PlaybackState.PAUSED) return
        track.play()
        _playbackState.value = PlaybackState.PLAYING
        if (amplitudeJob?.isActive != true) {
            startAmplitudePolling(playbackGeneration)
        }
        if (writerJob?.isActive != true && writerOffset < renderedBytes.size) {
            startWriter(track, renderedBytes, playbackGeneration)
        }
    }

    override fun stop() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        val track = audioTrack
        audioTrack = null
        writerJob?.cancel()
        writerJob = null
        track?.stop()
        track?.release()
        _playbackState.value = PlaybackState.STOPPED
        _amplitude.value = 0f
        _currentPositionMs.value = 0L
        _totalDurationMs.value = 0L
        _spectrumBands.value = FloatArray(BAND_COUNT)
        smoothedBands.fill(0f)
    }

    private fun startWriter(track: AudioTrack, bytes: ByteArray, generation: Int) {
        writerJob = scope.launch(Dispatchers.IO) {
            while (isActive && generation == playbackGeneration && writerOffset < bytes.size) {
                val chunk = (bytes.size - writerOffset).coerceAtMost(WRITE_CHUNK_BYTES)
                val written = try {
                    track.write(bytes, writerOffset, chunk)
                } catch (e: IllegalStateException) {
                    break
                }
                when {
                    written > 0 -> writerOffset += written
                    written == 0 -> delay(1)   // buffer full (track paused), yield and retry
                    else -> break              // real error (negative code)
                }
            }
        }
    }

    private fun startAmplitudePolling(generation: Int) {
        amplitudeJob = scope.launch {
            while (isActive && generation == playbackGeneration) {
                val track = audioTrack
                if (track == null || _playbackState.value == PlaybackState.STOPPED) {
                    break
                }
                if (_playbackState.value == PlaybackState.PLAYING) {
                    val pos = try { track.playbackHeadPosition } catch (e: IllegalStateException) { break }
                    // Fallback: detect end-of-playback if onMarkerReached didn't fire
                    if (renderedSamples.isNotEmpty() && pos >= renderedSamples.size) {
                        if (generation == playbackGeneration) _playbackState.value = PlaybackState.STOPPED
                        break
                    }
                    _currentPositionMs.value = pos * 1000L / WaveformGenerator.SAMPLE_RATE
                    _amplitude.value = sampleAmplitude(pos)
                    _spectrumBands.value = computeSpectrumBands(pos)
                } else {
                    _amplitude.value = 0f
                }
                delay(AMPLITUDE_POLL_MS)
            }
            _amplitude.value = 0f
        }
    }

    private fun sampleAmplitude(framePosition: Int): Float {
        val samples = renderedSamples
        if (samples.isEmpty()) return 0f
        val start = framePosition.coerceIn(0, samples.size)
        val end = (start + AMPLITUDE_WINDOW_SAMPLES).coerceAtMost(samples.size)
        if (end <= start) return 0f
        var sumSquares = 0.0
        for (i in start until end) {
            val normalized = samples[i] / Short.MAX_VALUE.toFloat()
            sumSquares += normalized * normalized
        }
        val rms = sqrt(sumSquares / (end - start))
        return rms.toFloat().coerceIn(0f, 1f)
    }

    private fun computeSpectrumBands(framePosition: Int): FloatArray {
        val samples = renderedSamples
        if (samples.isEmpty()) return FloatArray(BAND_COUNT)

        val center = framePosition.coerceIn(0, samples.size)
        val start = (center - FFT_SIZE / 2).coerceAtLeast(0)
        val end = (start + FFT_SIZE).coerceAtMost(samples.size)
        val windowSize = end - start

        val real = FloatArray(FFT_SIZE)
        val imag = FloatArray(FFT_SIZE)
        for (i in 0 until windowSize) {
            val s = samples[start + i] / Short.MAX_VALUE.toFloat()
            val hann = 0.5f * (1f - cos(2.0 * PI * i / (windowSize - 1)).toFloat())
            real[i] = s * hann
        }

        fft(real, imag)

        val freqPerBin = WaveformGenerator.SAMPLE_RATE.toFloat() / FFT_SIZE
        val logMin = ln(BAND_FREQ_MIN.toDouble())
        val logRange = ln(BAND_FREQ_MAX.toDouble()) - logMin
        val result = FloatArray(BAND_COUNT)
        for (b in 0 until BAND_COUNT) {
            val freqLow = Math.E.pow(logMin + logRange * b / BAND_COUNT).toFloat()
            val freqHigh = Math.E.pow(logMin + logRange * (b + 1) / BAND_COUNT).toFloat()
            val binLow = (freqLow / freqPerBin).toInt().coerceIn(1, FFT_SIZE / 2 - 1)
            val binHigh = (freqHigh / freqPerBin).toInt().coerceIn(binLow + 1, FFT_SIZE / 2)
            var sum = 0f
            for (k in binLow until binHigh) {
                sum += sqrt(real[k] * real[k] + imag[k] * imag[k]) / (FFT_SIZE / 2)
            }
            val raw = (sum / (binHigh - binLow) * BAND_GAIN).coerceIn(0f, 1f)
            val alpha = if (raw > smoothedBands[b]) BAND_ATTACK else BAND_RELEASE
            smoothedBands[b] = smoothedBands[b] + alpha * (raw - smoothedBands[b])
            result[b] = smoothedBands[b]
        }
        return result
    }

    private fun fft(real: FloatArray, imag: FloatArray) {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                var tmp = real[i]; real[i] = real[j]; real[j] = tmp
                tmp = imag[i]; imag[i] = imag[j]; imag[j] = tmp
            }
        }
        var len = 2
        while (len <= n) {
            val halfLen = len / 2
            val ang = -2.0 * PI / len
            val wRe = cos(ang).toFloat()
            val wIm = sin(ang).toFloat()
            var i = 0
            while (i < n) {
                var curRe = 1f; var curIm = 0f
                for (k in 0 until halfLen) {
                    val uRe = real[i + k]; val uIm = imag[i + k]
                    val vRe = real[i + k + halfLen] * curRe - imag[i + k + halfLen] * curIm
                    val vIm = real[i + k + halfLen] * curIm + imag[i + k + halfLen] * curRe
                    real[i + k] = uRe + vRe; imag[i + k] = uIm + vIm
                    real[i + k + halfLen] = uRe - vRe; imag[i + k + halfLen] = uIm - vIm
                    val newCurRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe; curRe = newCurRe
                }
                i += len
            }
            len = len shl 1
        }
    }

    private class EngineState {
        var tempo: Int = DEFAULT_TEMPO_BPM
        var volume: Float = 1f
    }

    private fun render(command: SoundCommand, state: EngineState): ShortArray = when (command) {
        is SoundCommand.Play -> WaveformGenerator.sineWave(command.frequencyHz, command.durationMs, state.volume)
        is SoundCommand.Pause -> WaveformGenerator.silence(command.durationMs)
        is SoundCommand.Bit -> renderPattern(command.pattern, BIT_FREQUENCY_HZ, BIT_HIT_MS, state)
        is SoundCommand.Bass -> renderPattern(command.pattern, BASS_FREQUENCY_HZ, BASS_HIT_MS, state)
        is SoundCommand.Snare -> renderPattern(command.pattern, null, SNARE_HIT_MS, state)
        is SoundCommand.HiHat -> renderPattern(command.pattern, null, HIHAT_HIT_MS, state)
        is SoundCommand.Clap -> renderPattern(command.pattern, null, CLAP_HIT_MS, state)
        is SoundCommand.Tom -> renderPattern(command.pattern, TOM_FREQUENCY_HZ, TOM_HIT_MS, state)
        is SoundCommand.Crash -> renderPattern(command.pattern, null, CRASH_HIT_MS, state)
        is SoundCommand.Piano -> renderPiano(command.notes, state)
        is SoundCommand.Square -> WaveformGenerator.squareWave(command.frequencyHz, command.durationMs, state.volume)
        is SoundCommand.Noise -> WaveformGenerator.noise(command.durationMs, state.volume)
        is SoundCommand.Volume -> {
            state.volume = command.level
            ShortArray(0)
        }
        is SoundCommand.Tempo -> {
            state.tempo = command.bpm
            ShortArray(0)
        }
        is SoundCommand.Loop -> renderLoop(command, state)
        is SoundCommand.Repeat -> renderRepeat(command, state)
        is SoundCommand.WithVolume -> {
            val previous = state.volume
            state.volume = command.level
            val result = render(command.command, state)
            state.volume = previous
            result
        }
        is SoundCommand.WithTempo -> {
            val previous = state.tempo
            state.tempo = command.bpm
            val result = render(command.command, state)
            state.tempo = previous
            result
        }
        is SoundCommand.Reverse -> render(command.command, state).reversedArray()
        is SoundCommand.After -> ShortArray(0)
        is SoundCommand.AfterAll -> ShortArray(0)
    }

    private fun renderPattern(
        pattern: List<Boolean>,
        hitFrequencyHz: Float?,
        hitMs: Long,
        state: EngineState,
    ): ShortArray {
        val stepMs = stepDurationMs(state.tempo)
        val activeMs = hitMs.coerceAtMost(stepMs)
        val tailMs = stepMs - activeMs

        val segments = pattern.map { active ->
            if (!active) {
                WaveformGenerator.silence(stepMs)
            } else {
                val hit = if (hitFrequencyHz != null) {
                    WaveformGenerator.sineWave(hitFrequencyHz, activeMs, state.volume)
                } else {
                    WaveformGenerator.noise(activeMs, state.volume)
                }
                WaveformGenerator.concat(listOf(hit, WaveformGenerator.silence(tailMs)))
            }
        }
        return WaveformGenerator.concat(segments)
    }

    private fun renderPiano(notes: List<Int>, state: EngineState): ShortArray {
        val stepMs = stepDurationMs(state.tempo)
        val activeMs = PIANO_HIT_MS.coerceAtMost(stepMs)
        val tailMs = stepMs - activeMs

        val segments = notes.map { note ->
            val frequency = pianoFrequencyHz(note)
            if (frequency == null) {
                WaveformGenerator.silence(stepMs)
            } else {
                val hit = WaveformGenerator.sineWave(frequency, activeMs, state.volume)
                WaveformGenerator.concat(listOf(hit, WaveformGenerator.silence(tailMs)))
            }
        }
        return WaveformGenerator.concat(segments)
    }

    private fun pianoFrequencyHz(note: Int): Float? {
        if (note < 1 || note > 52) return null
        val whiteKeyOffsets = intArrayOf(0, 2, 4, 5, 7, 9, 11)
        val index = note - 1
        val midi = 48 + (index / 7) * 12 + whiteKeyOffsets[index % 7]
        return 440f * 2f.pow((midi - 69) / 12f)
    }

    private fun renderLoop(command: SoundCommand.Loop, state: EngineState): ShortArray {
        val base = render(command.command, state)
        val gap = WaveformGenerator.silence(command.intervalMs)
        val segments = (0 until command.times).map { index ->
            if (index == 0) base else WaveformGenerator.concat(listOf(gap, base))
        }
        return WaveformGenerator.concat(segments)
    }

    private fun renderRepeat(command: SoundCommand.Repeat, state: EngineState): ShortArray {
        val segments = (0 until command.times).map { render(command.command, state) }
        return WaveformGenerator.concat(segments)
    }

    private fun startOffsets(commands: List<SoundCommand>, tracks: List<ShortArray>): IntArray {
        val offsets = IntArray(commands.size)
        var base = 0
        var groupStart = 0
        var previousIndex = -1
        // Non-null means the NEXT sound command must start at this offset (from an `after` marker).
        var pendingAfterOffset: Int? = null

        for (i in commands.indices) {
            when (commands[i]) {
                is SoundCommand.After -> {
                    offsets[i] = 0
                    if (previousIndex >= 0) {
                        pendingAfterOffset = offsets[previousIndex] + tracks[previousIndex].size
                    }
                }
                is SoundCommand.AfterAll -> {
                    offsets[i] = 0
                    for (j in groupStart until i) {
                        if (commands[j] !is SoundCommand.After && commands[j] !is SoundCommand.AfterAll) {
                            base = maxOf(base, offsets[j] + tracks[j].size)
                        }
                    }
                    groupStart = i + 1
                    // Reset so `after` after `after_all` won't reference a pre-barrier command.
                    previousIndex = -1
                    pendingAfterOffset = null
                }
                else -> {
                    offsets[i] = pendingAfterOffset ?: base
                    pendingAfterOffset = null
                    previousIndex = i
                }
            }
        }
        return offsets
    }

    private fun pad(track: ShortArray, offset: Int): ShortArray {
        if (offset <= 0) return track
        val result = ShortArray(offset + track.size)
        track.copyInto(result, offset)
        return result
    }

    private fun stepDurationMs(bpm: Int): Long {
        val safeBpm = bpm.coerceAtLeast(1)
        return (60_000L / safeBpm / 2).coerceAtLeast(1L)
    }

    private fun toPcm16(samples: ShortArray): ByteArray {
        val buffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in samples) buffer.putShort(sample)
        return buffer.array()
    }
}
