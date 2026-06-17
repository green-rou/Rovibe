package com.greenrou.rovibe.data.sound

object SoundDurationCalculator {

    private const val DEFAULT_TEMPO_BPM = 120

    fun calculateDurationMs(script: String): Long {
        val commands = SoundCommandParser.parseScript(script)
        if (commands.isEmpty()) return 0L

        val state = CalcState()
        val durations = commands.map { renderDurationMs(it, state) }

        val offsets = LongArray(commands.size)
        var base = 0L
        var groupStart = 0
        var previousIndex = -1
        var pendingAfterOffset: Long? = null

        for (i in commands.indices) {
            when (commands[i]) {
                is SoundCommand.After -> {
                    offsets[i] = 0L
                    if (previousIndex >= 0) {
                        pendingAfterOffset = offsets[previousIndex] + durations[previousIndex]
                    }
                }
                is SoundCommand.AfterAll -> {
                    offsets[i] = 0L
                    for (j in groupStart until i) {
                        if (commands[j] !is SoundCommand.After && commands[j] !is SoundCommand.AfterAll) {
                            base = maxOf(base, offsets[j] + durations[j])
                        }
                    }
                    groupStart = i + 1
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

        return commands.indices.maxOfOrNull { i -> offsets[i] + durations[i] } ?: 0L
    }

    private class CalcState(
        var tempo: Int = DEFAULT_TEMPO_BPM,
        var volume: Float = 1f,
    )

    private fun renderDurationMs(cmd: SoundCommand, state: CalcState): Long = when (cmd) {
        is SoundCommand.Play       -> cmd.durationMs
        is SoundCommand.Pause      -> cmd.durationMs
        is SoundCommand.Noise      -> cmd.durationMs
        is SoundCommand.Square     -> cmd.durationMs
        is SoundCommand.Bit        -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.Bass       -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.Snare      -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.HiHat      -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.Clap       -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.Tom        -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.Crash      -> cmd.pattern.size * stepMs(state.tempo)
        is SoundCommand.Piano      -> cmd.notes.size * stepMs(state.tempo)
        is SoundCommand.Volume     -> { state.volume = cmd.level; 0L }
        is SoundCommand.Tempo      -> { state.tempo = cmd.bpm; 0L }
        is SoundCommand.After      -> 0L
        is SoundCommand.AfterAll   -> 0L
        is SoundCommand.Voice      -> 0L
        is SoundCommand.Loop       -> {
            val base = renderDurationMs(cmd.command, state)
            base * cmd.times + cmd.intervalMs * (cmd.times - 1)
        }
        is SoundCommand.Repeat     -> renderDurationMs(cmd.command, state) * cmd.times
        is SoundCommand.WithVolume -> {
            val prev = state.volume; state.volume = cmd.level
            val result = renderDurationMs(cmd.command, state)
            state.volume = prev; result
        }
        is SoundCommand.WithTempo  -> {
            val prev = state.tempo; state.tempo = cmd.bpm
            val result = renderDurationMs(cmd.command, state)
            state.tempo = prev; result
        }
        is SoundCommand.Reverse    -> renderDurationMs(cmd.command, state)
        is SoundCommand.WithPitch  -> renderDurationMs(cmd.command, state)
        is SoundCommand.WithSpeed  -> (renderDurationMs(cmd.command, state) / cmd.factor.coerceAtLeast(0.01f)).toLong()
        is SoundCommand.WithFadeIn -> renderDurationMs(cmd.command, state)
        is SoundCommand.WithFadeOut -> renderDurationMs(cmd.command, state)
    }

    private fun stepMs(bpm: Int): Long =
        (60_000L / bpm.coerceAtLeast(1) / 2).coerceAtLeast(1L)
}
