package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

private val BarsColor = CommandColors["bars"] ?: Color(0xFF5EEAD4)
private const val BAR_GAP_FRACTION = 0.3f

@Composable
fun BarsVisualizer(bands: FloatArray, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val count = bands.size
        if (count == 0) return@Canvas
        val barWidth = size.width / count
        val gap = barWidth * BAR_GAP_FRACTION
        val drawWidth = barWidth - gap
        val maxBarHeight = size.height * 0.9f
        val minBarHeight = size.height * 0.04f
        for (i in 0 until count) {
            val barHeight = (minBarHeight + (maxBarHeight - minBarHeight) * bands[i].coerceIn(0f, 1f))
                .coerceAtLeast(minBarHeight)
            drawRect(
                color = BarsColor,
                topLeft = Offset(i * barWidth + gap / 2f, size.height - barHeight),
                size = Size(drawWidth, barHeight),
            )
        }
    }
}
