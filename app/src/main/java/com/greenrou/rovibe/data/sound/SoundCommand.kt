package com.greenrou.rovibe.data.sound

sealed interface SoundCommand {
    data class Play(val frequencyHz: Float, val durationMs: Long) : SoundCommand
    data class Pause(val durationMs: Long) : SoundCommand
    data class Bit(val pattern: List<Boolean>) : SoundCommand
    data class Bass(val pattern: List<Boolean>) : SoundCommand
    data class Snare(val pattern: List<Boolean>) : SoundCommand
    data class HiHat(val pattern: List<Boolean>) : SoundCommand
    data class Clap(val pattern: List<Boolean>) : SoundCommand
    data class Tom(val pattern: List<Boolean>) : SoundCommand
    data class Crash(val pattern: List<Boolean>) : SoundCommand
    data class Piano(val notes: List<Int>) : SoundCommand
    data class Square(val frequencyHz: Float, val durationMs: Long) : SoundCommand
    data class Noise(val durationMs: Long) : SoundCommand
    data class Volume(val level: Float) : SoundCommand
    data class Tempo(val bpm: Int) : SoundCommand
    data class Loop(val command: SoundCommand, val intervalMs: Long, val times: Int) : SoundCommand
    data class Repeat(val command: SoundCommand, val times: Int) : SoundCommand
    data class WithVolume(val command: SoundCommand, val level: Float) : SoundCommand
    data class WithTempo(val command: SoundCommand, val bpm: Int) : SoundCommand
    data class Reverse(val command: SoundCommand) : SoundCommand
    data class WithPitch(val command: SoundCommand, val factor: Float) : SoundCommand
    data class WithSpeed(val command: SoundCommand, val factor: Float) : SoundCommand
    data class WithFadeIn(val command: SoundCommand, val durationMs: Long) : SoundCommand
    data class WithFadeOut(val command: SoundCommand, val durationMs: Long) : SoundCommand
    data object After : SoundCommand
    data object AfterAll : SoundCommand
    data class Voice(val id: String) : SoundCommand
}
