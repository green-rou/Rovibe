package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greenrou.rovibe.R
import com.greenrou.rovibe.ui.screen.create.CreateViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreateScreen(
    itemId: String? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateViewModel = koinViewModel(parameters = { parametersOf(itemId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onVoicePermissionResult(granted) }

    LaunchedEffect(Unit) {
        viewModel.requestPermission.collect {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CreateTopBar(
                title = state.title ?: stringResource(R.string.create_title),
                playbackState = state.playbackState,
                onBack = onBack,
                onTogglePlayback = viewModel::togglePlayback,
                onSave = viewModel::save,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            CreateTerminalInput(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                amplitude = state.amplitude,
                spectrumBands = state.spectrumBands,
                waveAnchorOffsets = state.waveAnchorOffsets,
                barsAnchorOffsets = state.barsAnchorOffsets,
                pianoVisualizers = state.pianoVisualizers,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            )
            val sliderEdit = state.sliderEdit
            val voiceEdit = state.voiceEdit
            when {
                sliderEdit != null -> CreateSliderBar(
                    position = sliderEdit.position,
                    value = sliderEdit.valueText,
                    onPositionChange = viewModel::onSliderPositionChange,
                    onPositionChangeFinished = viewModel::onSliderPositionChangeFinished,
                    onDone = viewModel::onSliderDone,
                )
                voiceEdit != null -> CreateVoiceBar(
                    isRecording = voiceEdit.isRecording,
                    elapsedMs = voiceEdit.elapsedMs,
                    hasRecording = voiceEdit.id.isNotEmpty(),
                    onToggleRecord = viewModel::onVoiceToggleRecord,
                    onDelete = viewModel::onVoiceDelete,
                    onDone = viewModel::onVoiceDone,
                )
                else -> CreateSuggestionBar(
                    suggestions = state.suggestions,
                    parameterHint = state.parameterHint,
                    onSuggestionClick = viewModel::onSuggestionSelected,
                )
            }
        }
    }
}
