package com.greenrou.rovibe.ui.screen.composition_editor.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.R
import com.greenrou.rovibe.data.composition.CompositionTrack
import com.greenrou.rovibe.ui.theme.TerminalSubtext
import com.greenrou.rovibe.ui.theme.TerminalSurface
import com.greenrou.rovibe.ui.theme.TerminalText

@Composable
fun TrackHeader(
    track: CompositionTrack,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .width(HeaderWidth)
            .height(TrackHeight)
            .background(TerminalSurface)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = track.name,
            color = TerminalText,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.delete_track),
                tint = TerminalSubtext,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun TrackHeaderSpacer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(HeaderWidth)
            .height(TrackHeight)
            .background(TerminalSurface),
    )
}
