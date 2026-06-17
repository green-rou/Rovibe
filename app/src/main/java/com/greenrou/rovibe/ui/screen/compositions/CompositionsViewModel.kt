package com.greenrou.rovibe.ui.screen.compositions

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rovibe.data.SoundRepository
import com.greenrou.rovibe.data.composition.Composition
import com.greenrou.rovibe.data.composition.CompositionRepository
import com.greenrou.rovibe.data.composition.generateCompositionName
import com.greenrou.rovibe.data.sound.AudioTrackSoundEngine
import com.greenrou.rovibe.data.sound.SoundCommandRepository
import com.greenrou.rovibe.data.sound.SoundDurationCalculator
import com.greenrou.rovibe.data.sound.VoiceRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class CompositionsViewModel(
    private val repository: CompositionRepository,
    private val soundRepository: SoundRepository,
    private val voiceRecorder: VoiceRecorder,
) : ViewModel() {

    private val _playingId = MutableStateFlow<String?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _positionMs = MutableStateFlow(0L)
    private val _durationMs = MutableStateFlow(0L)

    val state: StateFlow<CompositionsState> = combine(
        repository.items,
        _playingId,
        _isPlaying,
        _positionMs,
        _durationMs,
    ) { items, playingId, isPlaying, positionMs, durationMs ->
        CompositionsState(
            items = items,
            playingId = playingId,
            isPlaying = isPlaying,
            playbackPositionMs = positionMs,
            playbackDurationMs = durationMs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompositionsState())

    fun createNew(): String {
        val composition = Composition(
            id = UUID.randomUUID().toString(),
            name = generateCompositionName(),
        )
        repository.create(composition)
        return composition.id
    }

    fun delete(id: String) {
        if (_playingId.value == id) stop()
        repository.delete(id)
    }

    fun rename(id: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val comp = repository.getById(id) ?: return
        repository.update(comp.copy(name = trimmed))
    }

    private val trackEngines = mutableMapOf<String, Pair<AudioTrackSoundEngine, SoundCommandRepository>>()
    private var playJob: Job? = null

    fun togglePlay(composition: Composition) {
        if (_playingId.value == composition.id && _isPlaying.value) {
            stop()
            return
        }
        stop()
        _playingId.value = composition.id
        _durationMs.value = calculateCompositionDurationMs(composition)
        _positionMs.value = 0L
        _isPlaying.value = true

        playJob = viewModelScope.launch {
            val barMs = (60_000.0 / composition.bpm) * 4
            val t0 = SystemClock.elapsedRealtime()
            coroutineScope {
                launch {
                    while (isActive) {
                        _positionMs.value = SystemClock.elapsedRealtime() - t0
                        delay(16L)
                    }
                }
                composition.tracks.forEach { track ->
                    if (track.patterns.isEmpty()) return@forEach
                    launch {
                        val (_, repo) = trackEngines.getOrPut(track.id) {
                            val e = AudioTrackSoundEngine(voiceRecorder)
                            e to SoundCommandRepository(e)
                        }
                        track.patterns.sortedBy { it.startBar }.forEach { block ->
                            val waitMs = (block.startBar * barMs).toLong() - (SystemClock.elapsedRealtime() - t0)
                            if (waitMs > 0) delay(waitMs)
                            if (!isActive) return@forEach
                            val sound = soundRepository.getById(block.soundId) ?: return@forEach
                            repo.play(sound.content)
                        }
                    }
                }
            }
            stop()
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        trackEngines.values.forEach { (_, repo) -> repo.stop() }
        trackEngines.clear()
        _isPlaying.value = false
        _positionMs.value = 0L
        _durationMs.value = 0L
        _playingId.value = null
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    private fun calculateCompositionDurationMs(composition: Composition): Long {
        val barMs = (60_000.0 / composition.bpm) * 4
        var maxMs = 0L
        composition.tracks.forEach { track ->
            track.patterns.forEach { block ->
                val sound = soundRepository.getById(block.soundId)
                val soundMs = if (sound != null) {
                    SoundDurationCalculator.calculateDurationMs(sound.content)
                } else 0L
                val endMs = (block.startBar * barMs).toLong() + soundMs
                if (endMs > maxMs) maxMs = endMs
            }
        }
        return maxMs
    }
}
