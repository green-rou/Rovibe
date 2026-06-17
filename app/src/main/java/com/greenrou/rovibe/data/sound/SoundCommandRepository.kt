package com.greenrou.rovibe.data.sound

import kotlinx.coroutines.flow.StateFlow

class SoundCommandRepository(private val engine: SoundEngine) {

    val playbackState: StateFlow<PlaybackState> = engine.playbackState
    val amplitude: StateFlow<Float> = engine.amplitude
    val spectrumBands: StateFlow<FloatArray> = engine.spectrumBands
    val currentPositionMs: StateFlow<Long> = engine.currentPositionMs
    val totalDurationMs: StateFlow<Long> = engine.totalDurationMs

    fun play(script: String) {
        engine.play(SoundCommandParser.parseScript(script))
    }

    fun pause() {
        engine.pause()
    }

    fun resume() {
        engine.resume()
    }

    fun stop() {
        engine.stop()
    }
}
