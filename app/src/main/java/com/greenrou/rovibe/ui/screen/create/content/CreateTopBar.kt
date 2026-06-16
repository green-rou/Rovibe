package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.greenrou.rovibe.R
import com.greenrou.rovibe.data.sound.PlaybackState
import com.greenrou.rovibe.ui.component.PlayPauseIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTopBar(
    title: String,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSave: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        actions = {
            val isPlaying = playbackState == PlaybackState.PLAYING
            IconButton(onClick = onTogglePlayback) {
                PlayPauseIcon(
                    isPlaying = isPlaying,
                    tint = LocalContentColor.current,
                    contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                )
            }
            TextButton(onClick = onSave) {
                Text(text = stringResource(R.string.save))
            }
        },
    )
}
