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
private const val DEFAULT_TEMPO_BPM = 120

class AudioTrackSoundEngine : SoundEngine {

    private var audioTrack: AudioTrack? = null
    private var playbackGeneration = 0
    private var renderedSamples: ShortArray = ShortArray(0)
    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    override fun play(commands: List<SoundCommand>) {
        stop()
        playbackGeneration++
        val generation = playbackGeneration

        val state = EngineState()
        val tracks = commands.map { render(it, state) }
        val samples = WaveformGenerator.mix(tracks)
        if (samples.isEmpty()) return
        renderedSamples = samples

        val bytes = toPcm16(samples)
        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(WaveformGenerator.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bytes.size,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        ).apply {
            setNotificationMarkerPosition(samples.size)
            setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack) {
                    if (generation == playbackGeneration) {
                        _playbackState.value = PlaybackState.STOPPED
                    }
                }

                override fun onPeriodicNotification(track: AudioTrack) = Unit
            })
            write(bytes, 0, bytes.size)
            play()
        }
        _playbackState.value = PlaybackState.PLAYING
        startAmplitudePolling(generation)
    }

    override fun pause() {
        val track = audioTrack ?: return
        if (_playbackState.value != PlaybackState.PLAYING) return
        track.pause()
        _playbackState.value = PlaybackState.PAUSED
        _amplitude.value = 0f
    }

    override fun resume() {
        val track = audioTrack ?: return
        if (_playbackState.value != PlaybackState.PAUSED) return
        track.play()
        _playbackState.value = PlaybackState.PLAYING
    }

    override fun stop() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        audioTrack?.run {
            stop()
            release()
        }
        audioTrack = null
        _playbackState.value = PlaybackState.STOPPED
        _amplitude.value = 0f
    }

    private fun startAmplitudePolling(generation: Int) {
        amplitudeJob = scope.launch {
            while (isActive && generation == playbackGeneration) {
                val track = audioTrack
                if (track == null || _playbackState.value == PlaybackState.STOPPED) {
                    break
                }
                _amplitude.value = if (_playbackState.value == PlaybackState.PLAYING) {
                    sampleAmplitude(track.playbackHeadPosition)
                } else {
                    0f
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
