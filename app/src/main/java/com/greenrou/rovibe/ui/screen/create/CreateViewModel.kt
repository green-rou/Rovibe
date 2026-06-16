package com.greenrou.rovibe.ui.screen.create

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.SoundRepository
import com.greenrou.rovibe.data.sound.PlaybackState
import com.greenrou.rovibe.data.sound.SoundCommandRepository
import com.greenrou.rovibe.data.sound.SoundCommandSpec
import com.greenrou.rovibe.data.sound.SoundCommandSpecs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private val WaveAnchorRegex = Regex("""(?i)\bwave\s*\(\s*\)""")
private val BarsAnchorRegex = Regex("""(?i)\bbars\s*\(\s*\)""")
private val PianoAnchorRegex = Regex("""(?i)\bpiano\s*\(([^)]*)\)""")
private val GlobalTempoRegex = Regex("""(?i)(?:^|\n)\s*tempo\s*\((\d+)\)""")
private val TempoModifierRegex = Regex("""(?i)\.tempo\s*\((\d+)\)""")

class CreateViewModel(
    private val repository: SoundRepository,
    private val soundCommandRepository: SoundCommandRepository,
    itemId: String? = null,
) : ViewModel() {

    private val editingItem = itemId?.let { repository.getById(it) }

    private val _state = MutableStateFlow(
        CreateState(
            input = TextFieldValue(editingItem?.content ?: ""),
            title = editingItem?.name,
            waveAnchorOffsets = findAllAnchorOffsets(editingItem?.content ?: "", WaveAnchorRegex),
            barsAnchorOffsets = findAllAnchorOffsets(editingItem?.content ?: "", BarsAnchorRegex),
            pianoVisualizers = findPianoVisualizers(editingItem?.content ?: ""),
        )
    )

    val state: StateFlow<CreateState> = combine(
        _state,
        soundCommandRepository.playbackState,
        soundCommandRepository.amplitude,
        soundCommandRepository.spectrumBands,
    ) { state, playback, amplitude, bands ->
        state.copy(playbackState = playback, amplitude = amplitude, spectrumBands = bands)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _state.value)

    fun onInputChange(input: TextFieldValue) {
        stopPlaybackIfNeeded()
        _state.update {
            val hint = parameterHintFor(input)
            it.copy(
                input = input,
                suggestions = suggestionsFor(input),
                parameterHint = hint,
                sliderEdit = resolveSliderEdit(input, hint),
                waveAnchorOffsets = findAllAnchorOffsets(input.text, WaveAnchorRegex),
                barsAnchorOffsets = findAllAnchorOffsets(input.text, BarsAnchorRegex),
                pianoVisualizers = findPianoVisualizers(input.text),
            )
        }
    }

    fun onSuggestionSelected(spec: SoundCommandSpec) {
        _state.update {
            val newInput = applySuggestion(it.input, spec)
            val hint = parameterHintFor(newInput)
            it.copy(
                input = newInput,
                suggestions = emptyList(),
                parameterHint = hint,
                sliderEdit = resolveSliderEdit(newInput, hint),
                waveAnchorOffsets = findAllAnchorOffsets(newInput.text, WaveAnchorRegex),
                barsAnchorOffsets = findAllAnchorOffsets(newInput.text, BarsAnchorRegex),
                pianoVisualizers = findPianoVisualizers(newInput.text),
            )
        }
    }

    fun onSliderPositionChange(position: Float) {
        stopPlaybackIfNeeded()
        _state.update { state ->
            val current = state.sliderEdit ?: return@update state
            val clamped = position.coerceIn(0f, 1f)
            val valueText = formatSliderValue(clamped)
            val replacement = "slider($valueText)"
            val text = state.input.text
            val range = current.range
            val newText = text.substring(0, range.start) + replacement + text.substring(range.end)
            val newRange = TextRange(range.start, range.start + replacement.length)
            state.copy(
                input = TextFieldValue(newText, selection = TextRange(newRange.end)),
                sliderEdit = current.copy(range = newRange, position = clamped, valueText = valueText),
                suggestions = emptyList(),
                parameterHint = null,
            )
        }
    }

    fun onSliderPositionChangeFinished() {
        soundCommandRepository.play(_state.value.input.text)
    }

    fun onSliderDone() {
        val text = _state.value.input.text
        _state.update {
            it.copy(
                sliderEdit = null,
                suggestions = suggestionsFor(it.input),
                parameterHint = parameterHintFor(it.input),
            )
        }
        soundCommandRepository.play(text)
    }

    private fun stopPlaybackIfNeeded() {
        if (soundCommandRepository.playbackState.value != PlaybackState.STOPPED) {
            soundCommandRepository.stop()
        }
    }

    fun togglePlayback() {
        when (soundCommandRepository.playbackState.value) {
            PlaybackState.PLAYING -> soundCommandRepository.pause()
            PlaybackState.PAUSED -> soundCommandRepository.resume()
            PlaybackState.STOPPED -> soundCommandRepository.play(_state.value.input.text)
        }
    }

    fun save() {
        val text = _state.value.input.text
        val current = editingItem
        if (current != null) {
            repository.update(current.copy(content = text))
        } else {
            val now = LocalDateTime.now()
            repository.add(
                SoundItem(
                    id = UUID.randomUUID().toString(),
                    name = SoundItem.generateName(now),
                    createdAt = now,
                    content = text,
                )
            )
        }
        _state.update { it.copy(isSaved = true) }
    }

    override fun onCleared() {
        soundCommandRepository.stop()
        super.onCleared()
    }

    private fun suggestionsFor(value: TextFieldValue): List<SoundCommandSpec> {
        if (value.selection.start != value.selection.end) return emptyList()
        val cursor = value.selection.start
        val lineStart = value.text.lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
        if (value.text.substring(lineStart).trimStart().startsWith("#")) return emptyList()
        return SoundCommandSpecs.matching(currentWord(value))
    }

    private fun parameterHintFor(value: TextFieldValue): SoundCommandSpec? =
        findEnclosingCommand(value)?.spec

    private fun findEnclosingCommand(value: TextFieldValue): EnclosingCommand? {
        val text = value.text
        var index = value.selection.end - 1
        var depth = 0
        while (index >= 0) {
            when (text[index]) {
                '\n' -> return null
                ')' -> depth++
                '(' -> {
                    if (depth == 0) {
                        var nameStart = index
                        while (nameStart > 0 && isWordChar(text[nameStart - 1])) nameStart--
                        val name = text.substring(nameStart, index)
                        val spec = SoundCommandSpecs.ALL.find { it.name.equals(name, ignoreCase = true) }
                            ?: return null
                        return EnclosingCommand(spec, nameStart, index)
                    }
                    depth--
                }
            }
            index--
        }
        return null
    }

    private fun resolveSliderEdit(value: TextFieldValue, hint: SoundCommandSpec?): SliderEdit? {
        if (hint == null || !hint.name.equals("slider", ignoreCase = true)) return null
        val enclosing = findEnclosingCommand(value) ?: return null
        val closeParen = value.text.indexOf(')', enclosing.openParen + 1)
        if (closeParen < 0) return null
        val range = TextRange(enclosing.nameStart, closeParen + 1)
        val existingValue = value.text.substring(enclosing.openParen + 1, closeParen)
        val position = positionForSliderValue(existingValue)
        return SliderEdit(range, position, formatSliderValue(position))
    }

    private fun findAllAnchorOffsets(text: String, regex: Regex): List<Int> {
        return regex.findAll(text).mapNotNull { match ->
            val lineStart = text.lastIndexOf('\n', match.range.first).let { if (it == -1) 0 else it + 1 }
            if (text.substring(lineStart).trimStart().startsWith("#")) return@mapNotNull null
            text.indexOf('\n', match.range.last).let { if (it == -1) text.length else it }
        }.toList()
    }

    private fun findPianoVisualizers(text: String): List<PianoVisualizer> {
        return PianoAnchorRegex.findAll(text).mapNotNull { match ->
            val lineStart = text.lastIndexOf('\n', match.range.first).let { if (it == -1) 0 else it + 1 }
            if (text.substring(lineStart).trimStart().startsWith("#")) return@mapNotNull null
            val lineEnd = text.indexOf('\n', match.range.last).let { if (it == -1) text.length else it }
            val notes = match.groupValues[1].trim().split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .map { it.toIntOrNull() ?: 0 }
            val line = text.substring(lineStart, lineEnd)
            val tempo = TempoModifierRegex.find(line)?.groupValues?.get(1)?.toIntOrNull()
                ?: GlobalTempoRegex.findAll(text.substring(0, lineStart))
                    .lastOrNull()?.groupValues?.get(1)?.toIntOrNull()
                ?: 120
            PianoVisualizer(anchorOffset = lineEnd, notes = notes, tempo = tempo.coerceAtLeast(1))
        }.toList()
    }

    private fun formatSliderValue(position: Float): String {
        val clamped = position.coerceIn(0f, 1f)
        return if (clamped <= 0.5f) {
            String.format(Locale.US, "%.1f", clamped * 2f)
        } else {
            (1 + (clamped - 0.5f) * 2f * 99f).roundToInt().toString()
        }
    }

    private fun positionForSliderValue(text: String): Float {
        val value = text.toFloatOrNull() ?: return 0f
        return if (text.contains('.')) {
            (value / 2f).coerceIn(0f, 0.5f)
        } else {
            (0.5f + (value - 1f) / 198f).coerceIn(0.5f, 1f)
        }
    }

    private fun applySuggestion(value: TextFieldValue, spec: SoundCommandSpec): TextFieldValue {
        val cursor = value.selection.start
        val start = wordStart(value.text, cursor)
        val newText = value.text.substring(0, start) + spec.usage + value.text.substring(cursor)

        val argsStart = start + spec.name.length + 1
        val argsEnd = start + spec.usage.length - 1
        val selection = if (argsEnd > argsStart) TextRange(argsStart, argsEnd) else TextRange(argsStart)
        return value.copy(text = newText, selection = selection)
    }

    private fun currentWord(value: TextFieldValue): String {
        val cursor = value.selection.start
        return value.text.substring(wordStart(value.text, cursor), cursor)
    }

    private fun wordStart(text: String, cursor: Int): Int {
        var index = cursor
        while (index > 0 && isWordChar(text[index - 1])) index--
        return index
    }

    private fun isWordChar(c: Char): Boolean = c.isLetter() || c == '_'
}

private data class EnclosingCommand(
    val spec: SoundCommandSpec,
    val nameStart: Int,
    val openParen: Int,
)
