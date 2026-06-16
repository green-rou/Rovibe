package com.greenrou.rovibe.data.sound

import androidx.annotation.StringRes
import com.greenrou.rovibe.R

data class SoundCommandSpec(
    val name: String,
    val usage: String,
    @StringRes val descriptionRes: Int,
)

object SoundCommandSpecs {
    val ALL = listOf(
        SoundCommandSpec("play",      "play(440hz, 1s)",        R.string.cmd_play_desc),
        SoundCommandSpec("pause",     "pause(1s)",              R.string.cmd_pause_desc),
        SoundCommandSpec("bit",       "bit(1 0 1 0 1 0 0 1)",   R.string.cmd_bit_desc),
        SoundCommandSpec("bass",      "bass(1 0 1 0 1 0 0 1)",  R.string.cmd_bass_desc),
        SoundCommandSpec("snare",     "snare(1 0 1 0 1 0 0 1)", R.string.cmd_snare_desc),
        SoundCommandSpec("hihat",     "hihat(1 0 1 0 1 0 0 1)", R.string.cmd_hihat_desc),
        SoundCommandSpec("clap",      "clap(1 0 1 0 1 0 0 1)",  R.string.cmd_clap_desc),
        SoundCommandSpec("tom",       "tom(1 0 1 0 1 0 0 1)",   R.string.cmd_tom_desc),
        SoundCommandSpec("crash",     "crash(1 0 0 0 0 0 0 0)", R.string.cmd_crash_desc),
        SoundCommandSpec("square",    "square(440hz, 1s)",       R.string.cmd_square_desc),
        SoundCommandSpec("noise",     "noise(0.5s)",             R.string.cmd_noise_desc),
        SoundCommandSpec("volume",    "volume(0.8)",             R.string.cmd_volume_desc),
        SoundCommandSpec("tempo",     "tempo(120)",              R.string.cmd_tempo_desc),
        SoundCommandSpec("loop",      "loop(0.5s, 10times)",     R.string.cmd_loop_desc),
        SoundCommandSpec("repeat",    "repeat(5times)",          R.string.cmd_repeat_desc),
        SoundCommandSpec("reverse",   "reverse()",               R.string.cmd_reverse_desc),
        SoundCommandSpec("after",     "after()",                 R.string.cmd_after_desc),
        SoundCommandSpec("after_all", "after_all()",             R.string.cmd_after_all_desc),
        SoundCommandSpec("slider",    "slider()",                R.string.cmd_slider_desc),
        SoundCommandSpec("wave",      "wave()",                  R.string.cmd_wave_desc),
        SoundCommandSpec("bars",      "bars()",                  R.string.cmd_bars_desc),
        SoundCommandSpec("piano",     "piano(1 5 8 12)",         R.string.cmd_piano_desc),
    )

    fun matching(prefix: String): List<SoundCommandSpec> =
        if (prefix.isEmpty()) emptyList()
        else ALL.filter { it.name.startsWith(prefix, ignoreCase = true) }
}
