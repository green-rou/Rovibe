package com.greenrou.rovibe.ui.screen.home.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greenrou.rovibe.R
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.ui.screen.home.HomeViewModel
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
        if (state.items.isEmpty()) {
            HomeEmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            HomeSoundList(
                sounds = state.items,
                playingItemId = state.playingItemId,
                onOpenClick = { onItemClick(it.id) },
                onPlayToggle = viewModel::togglePlay,
                onRenameClick = { renamingItem = it },
                modifier = Modifier.padding(innerPadding),
            )
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
