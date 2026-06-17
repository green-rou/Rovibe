package com.greenrou.rovibe.data.sound

private val COMMAND_REGEX = Regex("""(\w+)\(([^)]*)\)""")
private val SLIDER_REGEX = Regex("""(?i)\bslider\(([^()]*)\)""")

object SoundCommandParser {

    fun parseScript(script: String): List<SoundCommand> {
        val rawLines = script.lines()
        val hasSolo = rawLines.any { it.trimStart().startsWith("!") }

        val lines: List<String> = if (!hasSolo) {
            rawLines
        } else {
            val selected = mutableListOf<String>()
            var takingModifiers = false
            for (line in rawLines) {
                val trimmed = line.trimStart()
                when {
                    trimmed.startsWith("!") -> {
                        selected.add(trimmed.removePrefix("!"))
                        takingModifiers = true
                    }
                    trimmed.startsWith(".") && takingModifiers -> selected.add(line)
                    trimmed.isEmpty() || trimmed.startsWith("#") -> {}
                    else -> takingModifiers = false
                }
            }
            selected
        }

        val commands = mutableListOf<SoundCommand>()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#") -> {}
                trimmed.startsWith(".") -> {
                    val last = commands.removeLastOrNull() ?: continue
                    commands.add(parseModifierLine(last, trimmed) ?: last)
                }
                else -> parseLine(line)?.let { commands.add(it) }
            }
        }
        return commands
    }

    private fun parseModifierLine(base: SoundCommand, line: String): SoundCommand? {
        val resolved = resolveSliders(line)
        val matches = COMMAND_REGEX.findAll(resolved).toList()
        if (matches.isEmpty()) return null
        var command = base
        for (match in matches) {
            command = parseModifier(command, match) ?: return command
        }
        return command
    }

    fun parseLine(line: String): SoundCommand? {
        val resolved = resolveSliders(line)
        val matches = COMMAND_REGEX.findAll(resolved).toList()
        if (matches.isEmpty()) return null

        var command = parseBase(matches.first()) ?: return null
        for (modifier in matches.drop(1)) {
            command = parseModifier(command, modifier) ?: return command
        }
        return command
    }

    private fun resolveSliders(line: String): String =
        SLIDER_REGEX.replace(line) { it.groupValues[1].ifEmpty { "0" } }

    private fun parseBase(match: MatchResult): SoundCommand? {
        val (name, args) = match.destructured
        val parts = args.split(",").map { it.trim() }
        return when (name.lowercase()) {
            "play" -> SoundCommand.Play(
                frequencyHz = parseHz(parts.getOrElse(0) { "440hz" }),
                durationMs = parseSeconds(parts.getOrElse(1) { "1s" }),
            )
            "pause" -> SoundCommand.Pause(parseSeconds(parts.getOrElse(0) { "1s" }))
            "bit" -> SoundCommand.Bit(parsePattern(args))
            "bass" -> SoundCommand.Bass(parsePattern(args))
            "snare" -> SoundCommand.Snare(parsePattern(args))
            "hihat" -> SoundCommand.HiHat(parsePattern(args))
            "clap" -> SoundCommand.Clap(parsePattern(args))
            "tom" -> SoundCommand.Tom(parsePattern(args))
            "crash" -> SoundCommand.Crash(parsePattern(args))
            "piano" -> SoundCommand.Piano(parseNotes(args))
            "square" -> SoundCommand.Square(
                frequencyHz = parseHz(parts.getOrElse(0) { "440hz" }),
                durationMs = parseSeconds(parts.getOrElse(1) { "1s" }),
            )
            "noise" -> SoundCommand.Noise(parseSeconds(parts.getOrElse(0) { "1s" }))
            "volume" -> SoundCommand.Volume(parts.getOrElse(0) { "1" }.toFloatOrNull() ?: 1f)
            "tempo" -> SoundCommand.Tempo(parts.getOrElse(0) { "120" }.toIntOrNull() ?: 120)
            "after" -> SoundCommand.After
            "after_all" -> SoundCommand.AfterAll
            "voice" -> SoundCommand.Voice(parts.getOrElse(0) { "" }.trim())
            else -> null
        }
    }

    private fun parseModifier(base: SoundCommand, match: MatchResult): SoundCommand? {
        val (name, args) = match.destructured
        val parts = args.split(",").map { it.trim() }
        return when (name.lowercase()) {
            "loop" -> SoundCommand.Loop(
                command = base,
                intervalMs = parseSeconds(parts.getOrElse(0) { "0s" }),
                times = parseTimes(parts.getOrElse(1) { "1times" }),
            )
            "repeat" -> SoundCommand.Repeat(
                command = base,
                times = parseTimes(parts.getOrElse(0) { "1times" }),
            )
            "volume" -> SoundCommand.WithVolume(
                command = base,
                level = parts.getOrElse(0) { "1" }.toFloatOrNull() ?: 1f,
            )
            "tempo" -> SoundCommand.WithTempo(
                command = base,
                bpm = parts.getOrElse(0) { "120" }.toIntOrNull() ?: 120,
            )
            "reverse" -> SoundCommand.Reverse(base)
            "pitch" -> SoundCommand.WithPitch(base, parts.getOrElse(0) { "1" }.toFloatOrNull() ?: 1f)
            "speed" -> SoundCommand.WithSpeed(base, parts.getOrElse(0) { "1" }.toFloatOrNull() ?: 1f)
            "fadein" -> SoundCommand.WithFadeIn(base, parseSeconds(parts.getOrElse(0) { "0.5s" }))
            "fadeout" -> SoundCommand.WithFadeOut(base, parseSeconds(parts.getOrElse(0) { "0.5s" }))
            else -> null
        }
    }

    private fun parsePattern(args: String): List<Boolean> =
        args.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it == "1" }

    private fun parseNotes(args: String): List<Int> =
        args.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }

    private fun parseSeconds(token: String): Long {
        val seconds = token.trim().lowercase().removeSuffix("s").toDoubleOrNull() ?: 0.0
        return (seconds * 1000).toLong()
    }

    private fun parseHz(token: String): Float =
        token.trim().lowercase().removeSuffix("hz").toFloatOrNull() ?: 440f

    private fun parseTimes(token: String): Int =
        token.trim().lowercase().removeSuffix("times").toIntOrNull() ?: 1
}
