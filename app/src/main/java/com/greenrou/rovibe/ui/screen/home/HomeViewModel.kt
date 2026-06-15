package com.greenrou.rovibe.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.SoundRepository
import com.greenrou.rovibe.data.sound.PlaybackState
import com.greenrou.rovibe.data.sound.SoundCommandRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repository: SoundRepository,
    private val soundCommandRepository: SoundCommandRepository,
) : ViewModel() {

    private val _playingItemId = MutableStateFlow<String?>(null)

    val state: StateFlow<HomeState> = combine(
        repository.items,
        soundCommandRepository.playbackState,
        _playingItemId,
    ) { items, playback, playingId ->
        HomeState(
            items = items,
            playingItemId = if (playback == PlaybackState.STOPPED) null else playingId,
            playbackState = playback,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun togglePlay(item: SoundItem) {
        val playback = soundCommandRepository.playbackState.value
        if (_playingItemId.value == item.id) {
            when (playback) {
                PlaybackState.PLAYING -> soundCommandRepository.pause()
                PlaybackState.PAUSED -> soundCommandRepository.resume()
                PlaybackState.STOPPED -> soundCommandRepository.play(item.content)
            }
        } else {
            soundCommandRepository.play(item.content)
            _playingItemId.value = item.id
        }
    }

    fun rename(id: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) repository.rename(id, trimmed)
    }

    override fun onCleared() {
        soundCommandRepository.stop()
        super.onCleared()
    }
}
