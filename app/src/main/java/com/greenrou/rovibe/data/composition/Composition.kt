package com.greenrou.rovibe.data.composition

import java.time.LocalDateTime

data class Composition(
    val id: String,
    val name: String,
    val bpm: Int = 120,
    val tracks: List<CompositionTrack> = emptyList(),
    val createdAt: String = LocalDateTime.now().toString(),
)

fun generateCompositionName(): String {
    val now = LocalDateTime.now()
    return "Comp_%04d%02d%02d_%02d%02d%02d".format(
        now.year, now.monthValue, now.dayOfMonth,
        now.hour, now.minute, now.second,
    )
}
