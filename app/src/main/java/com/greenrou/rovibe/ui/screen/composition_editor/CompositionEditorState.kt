package com.greenrou.rovibe.ui.screen.composition_editor

import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.composition.Composition

enum class CompositionPlaybackState { STOPPED, PLAYING }

data class CompositionEditorState(
    val composition: Composition = Composition(id = "", name = ""),
    val playbackState: CompositionPlaybackState = CompositionPlaybackState.STOPPED,
    val sounds: List<SoundItem> = emptyList(),
    val pickingForTrack: String? = null,
    val pickingAtBar: Float = 0f,
    val playbackPositionBar: Float = 0f,
)
