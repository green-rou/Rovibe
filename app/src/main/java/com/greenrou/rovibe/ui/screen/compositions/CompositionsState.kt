package com.greenrou.rovibe.ui.screen.compositions

import com.greenrou.rovibe.data.composition.Composition

data class CompositionsState(
    val items: List<Composition> = emptyList(),
    val playingId: String? = null,
    val isPlaying: Boolean = false,
    val playbackPositionMs: Long = 0,
    val playbackDurationMs: Long = 0,
)
