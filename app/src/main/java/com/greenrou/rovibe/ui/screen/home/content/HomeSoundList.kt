package com.greenrou.rovibe.ui.screen.home.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.sound.PlaybackState

private val CardShape = RoundedCornerShape(16.dp)
private val DeleteReadyColor = Color(0xFFCF3030)
private val DeleteIdleColor = Color(0xFF3A1A1A)

@Composable
fun HomeSoundList(
    sounds: List<SoundItem>,
    playingItemId: String?,
    playbackState: PlaybackState,
    playbackPositionMs: Long,
    playbackDurationMs: Long,
    onOpenClick: (SoundItem) -> Unit,
    onPlayToggle: (SoundItem) -> Unit,
    onRenameClick: (SoundItem) -> Unit,
    onDeleteClick: (SoundItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = sounds, key = { it.id }) { item ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteClick(item)
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                modifier = Modifier.animateItem(),
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
                backgroundContent = { DeleteBackground(dismissState) },
            ) {
                val isThisItem = item.id == playingItemId
                SoundListItem(
                    item = item,
                    isPlaying = isThisItem && playbackState == PlaybackState.PLAYING,
                    positionMs = if (isThisItem) playbackPositionMs else 0L,
                    durationMs = if (isThisItem) playbackDurationMs else 0L,
                    onOpenClick = { onOpenClick(item) },
                    onPlayToggle = { onPlayToggle(item) },
                    onRenameClick = { onRenameClick(item) },
                )
            }
        }
    }
}

@Composable
private fun DeleteBackground(state: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        targetValue = if (state.targetValue == SwipeToDismissBoxValue.EndToStart)
            DeleteReadyColor else DeleteIdleColor,
        label = "delete_bg",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CardShape)
            .background(color),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = 20.dp),
        )
    }
}
