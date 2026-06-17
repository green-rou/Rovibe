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
    private val _pendingDelete = MutableStateFlow<SoundItem?>(null)

    val state: StateFlow<HomeState> = combine(
        combine(
            repository.items,
            soundCommandRepository.playbackState,
            _playingItemId,
            _pendingDelete,
        ) { items, playback, playingId, pending ->
            HomeState(
                items = items,
                playingItemId = if (playback == PlaybackState.STOPPED) null else playingId,
                playbackState = playback,
                pendingDelete = pending,
            )
        },
        soundCommandRepository.currentPositionMs,
        soundCommandRepository.totalDurationMs,
    ) { base, positionMs, durationMs ->
        base.copy(
            playbackPositionMs = positionMs,
            playbackDurationMs = durationMs,
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

    fun delete(item: SoundItem) {
        if (_playingItemId.value == item.id) soundCommandRepository.stop()
        repository.delete(item.id)
        _pendingDelete.value = item
    }

    fun undoDelete() {
        _pendingDelete.value?.let { repository.add(it) }
        _pendingDelete.value = null
    }

    fun dismissUndo() {
        _pendingDelete.value = null
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
