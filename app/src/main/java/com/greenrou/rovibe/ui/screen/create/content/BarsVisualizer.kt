package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val BarsColor = CommandColors["bars"] ?: Color(0xFF5EEAD4)
private const val BAR_COUNT = 24
private const val BAR_GAP_FRACTION = 0.35f
private const val BAR_PHASE_STEP = 0.5f

@Composable
fun BarsVisualizer(amplitude: Float, modifier: Modifier = Modifier) {
    val animatedAmplitude by animateFloatAsState(
        targetValue = amplitude.coerceIn(0f, 1f),
        label = "barsAmplitude",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "barsPhase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
        ),
        label = "barsPhaseAnim",
    )

    Canvas(modifier = modifier) {
        val barWidth = size.width / BAR_COUNT
        val gap = barWidth * BAR_GAP_FRACTION
        val drawWidth = barWidth - gap
        val maxBarHeight = size.height * 0.9f
        val minBarHeight = size.height * 0.06f
        for (i in 0 until BAR_COUNT) {
            val wobble = abs(sin(phase + i * BAR_PHASE_STEP))
            val heightFraction = (0.15f + 0.85f * wobble) * animatedAmplitude
            val barHeight = (minBarHeight + (maxBarHeight - minBarHeight) * heightFraction)
                .coerceAtLeast(minBarHeight)
            val x = i * barWidth + gap / 2f
            drawRect(
                color = BarsColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(drawWidth, barHeight),
            )
        }
    }
}
