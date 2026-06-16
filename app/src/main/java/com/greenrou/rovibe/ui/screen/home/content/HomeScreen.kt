package com.greenrou.rovibe.ui.screen.home.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greenrou.rovibe.R
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.ui.screen.home.HomeViewModel
import com.greenrou.rovibe.ui.theme.TerminalGreen
import com.greenrou.rovibe.ui.theme.TerminalSubtext
import com.greenrou.rovibe.ui.theme.TerminalSurface
import com.greenrou.rovibe.ui.theme.TerminalText
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onItemClick: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var renamingItem by remember { mutableStateOf<SoundItem?>(null) }

    LaunchedEffect(state.pendingDelete) {
        if (state.pendingDelete != null) {
            delay(4_000)
            viewModel.dismissUndo()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = { HomeTopBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_record),
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.items.isEmpty()) {
                HomeEmptyState(modifier = Modifier.padding(innerPadding))
            } else {
                HomeSoundList(
                    sounds = state.items,
                    playingItemId = state.playingItemId,
                    playbackState = state.playbackState,
                    onOpenClick = { onItemClick(it.id) },
                    onPlayToggle = viewModel::togglePlay,
                    onRenameClick = { renamingItem = it },
                    onDeleteClick = viewModel::delete,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            AnimatedVisibility(
                visible = state.pendingDelete != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 8.dp,
                    ),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                UndoDeleteSnackbar(
                    itemName = state.pendingDelete?.name.orEmpty(),
                    onUndo = viewModel::undoDelete,
                    onDismiss = viewModel::dismissUndo,
                )
            }
        }
    }

    renamingItem?.let { item ->
        RenameDialog(
            initialName = item.name,
            onConfirm = { newName ->
                viewModel.rename(item.id, newName)
                renamingItem = null
            },
            onDismiss = { renamingItem = null },
        )
    }
}

@Composable
private fun UndoDeleteSnackbar(
    itemName: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalSurface)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.snackbar_deleted, itemName),
            color = TerminalText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onUndo) {
            Text(
                text = stringResource(R.string.snackbar_restore),
                color = TerminalGreen,
                fontFamily = FontFamily.Monospace,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = TerminalSubtext,
            )
        }
    }
}
