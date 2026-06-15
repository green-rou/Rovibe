package com.greenrou.rovibe.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PlayPauseIcon(
    isPlaying: Boolean,
    tint: Color,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    Canvas(modifier = semanticsModifier.size(24.dp)) {
        if (isPlaying) {
            val barWidth = size.width * 0.28f
            val gap = size.width * 0.16f
            val barHeight = size.height * 0.9f
            val top = (size.height - barHeight) / 2f
            drawRect(
                color = tint,
                topLeft = Offset(size.width / 2f - gap / 2f - barWidth, top),
                size = Size(barWidth, barHeight),
            )
            drawRect(
                color = tint,
                topLeft = Offset(size.width / 2f + gap / 2f, top),
                size = Size(barWidth, barHeight),
            )
        } else {
            val path = Path().apply {
                moveTo(size.width * 0.22f, size.height * 0.08f)
                lineTo(size.width * 0.22f, size.height * 0.92f)
                lineTo(size.width * 0.88f, size.height * 0.5f)
                close()
            }
            drawPath(path, color = tint)
        }
    }
}
