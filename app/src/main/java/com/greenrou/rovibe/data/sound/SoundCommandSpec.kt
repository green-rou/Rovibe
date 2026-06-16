package com.greenrou.rovibe.data.sound

data class SoundCommandSpec(
    val name: String,
    val usage: String,
    val description: String,
)

object SoundCommandSpecs {
    val ALL = listOf(
        SoundCommandSpec("play", "play(440hz, 1s)", "Тон заданої частоти й тривалості"),
        SoundCommandSpec("pause", "pause(1s)", "Тиша заданої тривалості"),
        SoundCommandSpec("bit", "bit(1 0 1 0 1 0 0 1)", "Бітовий патерн: 1 — удар, 0 — пауза"),
        SoundCommandSpec("bass", "bass(1 0 1 0 1 0 0 1)", "Басовий патерн: 1 — удар, 0 — пауза"),
        SoundCommandSpec("snare", "snare(1 0 1 0 1 0 0 1)", "Патерн снейру: 1 — удар, 0 — пауза"),
        SoundCommandSpec("hihat", "hihat(1 0 1 0 1 0 0 1)", "Патерн хай-хету: 1 — удар, 0 — пауза"),
        SoundCommandSpec("clap", "clap(1 0 1 0 1 0 0 1)", "Патерн хлопка: 1 — удар, 0 — пауза"),
        SoundCommandSpec("tom", "tom(1 0 1 0 1 0 0 1)", "Патерн тома: 1 — удар, 0 — пауза"),
        SoundCommandSpec("crash", "crash(1 0 0 0 0 0 0 0)", "Патерн крешу: 1 — удар, 0 — пауза"),
        SoundCommandSpec("square", "square(440hz, 1s)", "Квадратна хвиля заданої частоти й тривалості"),
        SoundCommandSpec("noise", "noise(0.5s)", "Білий шум заданої тривалості"),
        SoundCommandSpec("volume", "volume(0.8)", "Рівень голосності від 0 до 1"),
        SoundCommandSpec("tempo", "tempo(120)", "Темп у ударах за хвилину"),
        SoundCommandSpec("loop", "loop(0.5s, 10times)", "Повторити з паузою між разами"),
        SoundCommandSpec("repeat", "repeat(5times)", "Повторити без паузи"),
        SoundCommandSpec("reverse", "reverse()", "Відтворити звук у зворотному напрямку"),
        SoundCommandSpec("after", "after()", "Наступні команди почнуться після завершення команди вище"),
        SoundCommandSpec("after_all", "after_all()", "Наступні команди почнуться після завершення всіх команд вище"),
        SoundCommandSpec("slider", "slider()", "Повзунок для вибору числового значення"),
        SoundCommandSpec("wave", "wave()", "Візуалізація хвилі на 3 рядки нижче"),
        SoundCommandSpec("bars", "bars()", "Стовпчаста візуалізація на 3 рядки нижче"),
        SoundCommandSpec("piano", "piano(1 5 8 12)", "Ноти піано-ролу від 1 до 52, через пробіл"),
    )

    fun matching(prefix: String): List<SoundCommandSpec> =
        if (prefix.isEmpty()) emptyList()
        else ALL.filter { it.name.startsWith(prefix, ignoreCase = true) }
}
