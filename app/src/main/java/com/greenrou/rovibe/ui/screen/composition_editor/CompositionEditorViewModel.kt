package com.greenrou.rovibe.ui.screen.composition_editor

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rovibe.data.SoundRepository
import com.greenrou.rovibe.data.composition.Composition
import com.greenrou.rovibe.data.composition.CompositionRepository
import com.greenrou.rovibe.data.composition.CompositionTrack
import com.greenrou.rovibe.data.composition.PatternBlock
import com.greenrou.rovibe.data.composition.generateCompositionName
import com.greenrou.rovibe.data.sound.AudioTrackSoundEngine
import com.greenrou.rovibe.data.sound.SoundCommandRepository
import com.greenrou.rovibe.data.sound.SoundDurationCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class CompositionEditorViewModel(
    private val compositionRepository: CompositionRepository,
    private val soundRepository: SoundRepository,
    compositionId: String?,
) : ViewModel() {

    private val _composition: MutableStateFlow<Composition>
    private val _playbackState = MutableStateFlow(CompositionPlaybackState.STOPPED)
    private val _pickingForTrack = MutableStateFlow<String?>(null)
    private val _pickingAtBar = MutableStateFlow(0f)
    private val _playbackPositionBar = MutableStateFlow(0f)

    init {
        val existing = compositionId?.let { compositionRepository.getById(it) }
        val composition = existing ?: Composition(
            id = UUID.randomUUID().toString(),
            name = generateCompositionName(),
        ).also { compositionRepository.create(it) }
        _composition = MutableStateFlow(composition)
    }

    val state: StateFlow<CompositionEditorState> = combine(
        _composition,
        _playbackState,
        soundRepository.items,
        _pickingForTrack,
        _pickingAtBar,
    ) { comp, playback, sounds, pickingTrack, pickingBar ->
        CompositionEditorState(
            composition = comp,
            playbackState = playback,
            sounds = sounds,
            pickingForTrack = pickingTrack,
            pickingAtBar = pickingBar,
        )
    }.combine(_playbackPositionBar) { s, pos ->
        s.copy(playbackPositionBar = pos)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompositionEditorState())

    fun addTrack() {
        val trackNumber = _composition.value.tracks.size + 1
        val newTrack = CompositionTrack(
            id = UUID.randomUUID().toString(),
            name = "Track $trackNumber",
        )
        mutate { it.copy(tracks = it.tracks + newTrack) }
    }

    fun deleteTrack(trackId: String) {
        trackEngines.remove(trackId)?.let { (_, repo) -> repo.stop() }
        mutate { it.copy(tracks = it.tracks.filter { t -> t.id != trackId }) }
    }

    fun setBpm(bpm: Int) {
        mutate { it.copy(bpm = bpm.coerceIn(40, 240)) }
    }

    fun startPickingPattern(trackId: String, bar: Float) {
        _pickingForTrack.value = trackId
        _pickingAtBar.value = bar
    }

    fun dismissPicker() {
        _pickingForTrack.value = null
    }

    fun placePattern(soundId: String) {
        val trackId = _pickingForTrack.value ?: return
        val bar = _pickingAtBar.value
        _pickingForTrack.value = null

        val bpm = _composition.value.bpm
        val barMs = (60_000.0 / bpm) * 4
        val sound = soundRepository.getById(soundId)
        val durationBars = if (sound != null) {
            val durationMs = SoundDurationCalculator.calculateDurationMs(sound.content)
            (durationMs / barMs).toFloat().coerceAtLeast(0.25f)
        } else {
            1f
        }

        val newBlock = PatternBlock(
            id = UUID.randomUUID().toString(),
            soundId = soundId,
            startBar = bar,
            durationBars = durationBars,
        )
        mutate { comp ->
            comp.copy(tracks = comp.tracks.map { track ->
                if (track.id == trackId) track.copy(patterns = track.patterns + newBlock)
                else track
            })
        }
    }

    fun movePattern(trackId: String, patternId: String, newStartBar: Float) {
        mutate { comp ->
            comp.copy(tracks = comp.tracks.map { track ->
                if (track.id != trackId) track
                else track.copy(patterns = track.patterns.map { block ->
                    if (block.id == patternId) block.copy(startBar = newStartBar.coerceAtLeast(0f))
                    else block
                })
            })
        }
    }

    fun removePattern(trackId: String, patternId: String) {
        mutate { comp ->
            comp.copy(tracks = comp.tracks.map { track ->
                if (track.id == trackId) track.copy(patterns = track.patterns.filter { it.id != patternId })
                else track
            })
        }
    }

    fun save() {
        compositionRepository.update(_composition.value)
    }

    // ── Playback ────────────────────────────────────────────────────────────

    private val trackEngines = mutableMapOf<String, Pair<AudioTrackSoundEngine, SoundCommandRepository>>()
    private var playJob: Job? = null

    fun play() {
        if (_playbackState.value == CompositionPlaybackState.PLAYING) {
            stop()
            return
        }
        playJob?.cancel()
        playJob = viewModelScope.launch {
            val comp = _composition.value
            val barMs = (60_000.0 / comp.bpm) * 4
            val t0 = SystemClock.elapsedRealtime()
            _playbackState.value = CompositionPlaybackState.PLAYING
            coroutineScope {
                // Position tracking at ~60fps
                launch {
                    while (isActive) {
                        _playbackPositionBar.value = ((SystemClock.elapsedRealtime() - t0) / barMs).toFloat()
                        delay(16L)
                    }
                }
                comp.tracks.forEach { track ->
                    if (track.patterns.isEmpty()) return@forEach
                    launch {
                        val (_, repo) = trackEngines.getOrPut(track.id) {
                            val e = AudioTrackSoundEngine()
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
            _playbackPositionBar.value = 0f
            _playbackState.value = CompositionPlaybackState.STOPPED
        }
    }

    fun stop() {
        playJob?.cancel()
        trackEngines.values.forEach { (_, repo) -> repo.stop() }
        _playbackPositionBar.value = 0f
        _playbackState.value = CompositionPlaybackState.STOPPED
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun mutate(transform: (Composition) -> Composition) {
        _composition.update(transform)
        compositionRepository.update(_composition.value)
    }
}
