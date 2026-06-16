package com.greenrou.rovibe.ui.screen.home

import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.sound.PlaybackState

data class HomeState(
    val items: List<SoundItem> = emptyList(),
    val playingItemId: String? = null,
    val playbackState: PlaybackState = PlaybackState.STOPPED,
    val pendingDelete: SoundItem? = null,
)
