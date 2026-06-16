package com.greenrou.rovibe.data.sound

import kotlinx.coroutines.flow.StateFlow

class SoundCommandRepository(private val engine: SoundEngine) {

    val playbackState: StateFlow<PlaybackState> = engine.playbackState
    val amplitude: StateFlow<Float> = engine.amplitude

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
