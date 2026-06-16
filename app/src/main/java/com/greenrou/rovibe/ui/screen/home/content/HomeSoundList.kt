package com.greenrou.rovibe.ui.screen.home.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rovibe.data.SoundItem

@Composable
fun HomeSoundList(
    sounds: List<SoundItem>,
    playingItemId: String?,
    onOpenClick: (SoundItem) -> Unit,
    onPlayToggle: (SoundItem) -> Unit,
    onRenameClick: (SoundItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = sounds, key = { it.id }) { item ->
            SoundListItem(
                item = item,
                isPlaying = item.id == playingItemId,
                onOpenClick = { onOpenClick(item) },
                onPlayToggle = { onPlayToggle(item) },
                onRenameClick = { onRenameClick(item) },
            )
        }
    }
}
