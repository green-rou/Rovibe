package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.R

private val BarBackground = Color(0xFF161B22)
private val TimerColor = Color(0xFF58A6FF)
private val RecordingColor = Color(0xFFFF6B6B)
private val IdleColor = Color(0xFF8B949E)

@Composable
fun CreateVoiceBar(
    isRecording: Boolean,
    elapsedMs: Long,
    hasRecording: Boolean,
    onToggleRecord: () -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BarBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleRecord) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) stringResource(R.string.stop) else stringResource(R.string.voice_record),
                tint = if (isRecording) RecordingColor else IdleColor,
            )
        }
        Text(
            text = formatElapsed(elapsedMs),
            color = if (isRecording) RecordingColor else TimerColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )
        if (hasRecording && !isRecording) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.voice_delete),
                    tint = IdleColor,
                )
            }
        }
        IconButton(onClick = onDone, enabled = !isRecording && hasRecording) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.done),
            )
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (ms % 1000) / 100
    return "%02d:%02d.%d".format(minutes, seconds, tenths)
}
