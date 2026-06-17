package com.greenrou.rovibe.data.sound

import kotlinx.coroutines.flow.StateFlow

enum class PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED,
}

interface SoundEngine {
    val playbackState: StateFlow<PlaybackState>
    val amplitude: StateFlow<Float>
    val spectrumBands: StateFlow<FloatArray>
    val currentPositionMs: StateFlow<Long>
    val totalDurationMs: StateFlow<Long>
    fun play(commands: List<SoundCommand>)
    fun pause()
    fun resume()
    fun stop()
}
