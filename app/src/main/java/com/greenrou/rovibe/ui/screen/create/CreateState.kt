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
    val amplitude: Float = 0f,
    val waveAnchorOffset: Int? = null,
)

data class SliderEdit(
    val range: TextRange,
    val position: Float,
    val valueText: String,
)
