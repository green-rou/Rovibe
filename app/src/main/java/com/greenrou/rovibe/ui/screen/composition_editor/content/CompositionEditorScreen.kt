package com.greenrou.rovibe.ui.screen.composition_editor.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greenrou.rovibe.R
import com.greenrou.rovibe.ui.screen.composition_editor.CompositionEditorViewModel
import com.greenrou.rovibe.ui.screen.composition_editor.CompositionPlaybackState
import com.greenrou.rovibe.ui.theme.TerminalGreen
import com.greenrou.rovibe.ui.theme.TerminalSubtext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompositionEditorScreen(
    compositionId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompositionEditorViewModel = koinViewModel(parameters = { parametersOf(compositionId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.save()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Text(
                        text = state.composition.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        IconButton(
                            onClick = { viewModel.setBpm(state.composition.bpm - 5) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Text(
                                text = "−",
                                color = TerminalSubtext,
                                fontSize = 16.sp,
                            )
                        }
                        Text(
                            text = "${state.composition.bpm}",
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier.width(36.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        IconButton(
                            onClick = { viewModel.setBpm(state.composition.bpm + 5) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Text(
                                text = "+",
                                color = TerminalSubtext,
                                fontSize = 16.sp,
                            )
                        }
                        Text(
                            text = stringResource(R.string.composition_editor_bpm),
                            color = TerminalSubtext,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }

                    IconButton(onClick = viewModel::play) {
                        Icon(
                            imageVector = if (state.playbackState == CompositionPlaybackState.PLAYING)
                                Icons.Default.Stop
                            else
                                Icons.Default.PlayArrow,
                            contentDescription = if (state.playbackState == CompositionPlaybackState.PLAYING)
                                stringResource(R.string.stop) else stringResource(R.string.play),
                            tint = if (state.playbackState == CompositionPlaybackState.PLAYING)
                                MaterialTheme.colorScheme.error else TerminalGreen,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        CompositionGrid(
            composition = state.composition,
            sounds = state.sounds,
            playbackState = state.playbackState,
            playbackPositionBar = state.playbackPositionBar,
            onCellTap = { trackId, bar -> viewModel.startPickingPattern(trackId, bar) },
            onPatternClick = { trackId, pattern -> viewModel.removePattern(trackId, pattern.id) },
            onMovePattern = { trackId, patternId, newStart -> viewModel.movePattern(trackId, patternId, newStart) },
            onAddTrack = viewModel::addTrack,
            onDeleteTrack = viewModel::deleteTrack,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (state.pickingForTrack != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissPicker,
            sheetState = bottomSheetState,
        ) {
            Text(
                text = stringResource(R.string.pick_sound_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.sounds.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.no_sounds_available),
                        color = TerminalSubtext,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(state.sounds, key = { it.id }) { sound ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = sound.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = previewLine(sound.content)?.let { preview ->
                                {
                                    Text(
                                        text = preview,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TerminalSubtext,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.placePattern(sound.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun previewLine(content: String): String? =
    content.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }
