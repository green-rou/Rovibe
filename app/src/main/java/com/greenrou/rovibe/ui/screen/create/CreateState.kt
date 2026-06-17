package com.greenrou.rovibe.ui.screen.create

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.greenrou.rovibe.data.sound.PlaybackState
import com.greenrou.rovibe.data.sound.SoundCommandSpec

data class CreateState(
    val input: TextFieldValue = TextFieldValue(""),
    val isSaved: Boolean = false,
    val suggestions: List<SoundCommandSpec> = emptyList(),
    val parameterHint: SoundCommandSpec? = null,
    val title: String? = null,
    val playbackState: PlaybackState = PlaybackState.STOPPED,
    val sliderEdit: SliderEdit? = null,
    val voiceEdit: VoiceEdit? = null,
    val amplitude: Float = 0f,
    val spectrumBands: FloatArray = FloatArray(24),
    val waveAnchorOffsets: List<Int> = emptyList(),
    val barsAnchorOffsets: List<Int> = emptyList(),
    val pianoVisualizers: List<PianoVisualizer> = emptyList(),
)

data class SliderEdit(
    val range: TextRange,
    val position: Float,
    val valueText: String,
)

data class VoiceEdit(
    val range: TextRange,
    val id: String,
    val isRecording: Boolean = false,
    val elapsedMs: Long = 0L,
)

data class PianoVisualizer(
    val anchorOffset: Int,
    val notes: List<Int>,
    val tempo: Int = 120,
)
