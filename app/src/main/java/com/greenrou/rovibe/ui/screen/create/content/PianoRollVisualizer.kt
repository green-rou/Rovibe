package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

private const val NOTES_VISIBLE = 8f
private const val MS_PER_NOTE = 350
private const val MIN_NOTE = 1
private const val MAX_NOTE = 52

@Composable
fun PianoRollVisualizer(
    notes: List<Int>,
    amplitude: Float,
    modifier: Modifier = Modifier,
) {
    val animatedAmplitude by animateFloatAsState(targetValue = amplitude, label = "amp")
    val infiniteTransition = rememberInfiniteTransition(label = "scroll")
    val scrollFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (MS_PER_NOTE * NOTES_VISIBLE).toInt(),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scroll",
    )

    Canvas(modifier = modifier) {
        if (notes.isEmpty()) return@Canvas

        val canvasWidth = size.width
        val canvasHeight = size.height
        val noteWidth = canvasWidth / NOTES_VISIBLE
        val noteHeight = (canvasHeight / MAX_NOTE * 2f).coerceAtLeast(4f)
        val cornerRadius = CornerRadius(4f, 4f)
        val gap = noteWidth * 0.15f
        val drawWidth = noteWidth - gap

        val totalNotes = notes.size
        val repeatCount = (NOTES_VISIBLE / totalNotes).toInt() + 2
        val baseOffset = scrollFraction * noteWidth * totalNotes

        repeat(repeatCount) { rep ->
            notes.forEachIndexed { index, note ->
                if (note < MIN_NOTE || note > MAX_NOTE) return@forEachIndexed
                val xBase = rep * noteWidth * totalNotes + index * noteWidth - baseOffset
                if (xBase + noteWidth < 0f || xBase > canvasWidth) return@forEachIndexed

                val yFraction = 1f - (note - MIN_NOTE).toFloat() / (MAX_NOTE - MIN_NOTE).toFloat()
                val y = yFraction * (canvasHeight - noteHeight)
                val alpha = 0.4f + animatedAmplitude * 0.6f

                drawRoundRect(
                    color = Color(0xFFB388FF).copy(alpha = alpha),
                    topLeft = Offset(xBase + gap / 2f, y),
                    size = Size(drawWidth, noteHeight),
                    cornerRadius = cornerRadius,
                )
            }
        }
    }
}
