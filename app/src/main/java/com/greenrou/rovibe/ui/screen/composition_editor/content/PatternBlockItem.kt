package com.greenrou.rovibe.ui.screen.composition_editor.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.ui.theme.TerminalGreen

private val BlockShape = RoundedCornerShape(4.dp)

@Composable
fun PatternBlockItem(
    soundName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp)
            .height(TrackHeight - 4.dp)
            .clip(BlockShape)
            .background(TerminalGreen.copy(alpha = 0.18f))
            .border(1.dp, TerminalGreen.copy(alpha = 0.6f), BlockShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            text = soundName,
            color = TerminalGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 12.sp,
        )
    }
}
