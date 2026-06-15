package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

private val WaveBackground = Color(0xFF0D1117)
private val WaveColor = CommandColors["wave"] ?: Color(0xFF39D2C0)

@Composable
fun WaveVisualizer(amplitude: Float, modifier: Modifier = Modifier) {
    val animatedAmplitude by animateFloatAsState(
        targetValue = amplitude.coerceIn(0f, 1f),
        label = "waveAmplitude",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
        ),
        label = "wavePhaseAnim",
    )

    Canvas(modifier = modifier.background(WaveBackground)) {
        val midY = size.height / 2f
        val maxAmplitudePx = size.height / 2f * 0.85f
        val amplitudePx = (maxAmplitudePx * animatedAmplitude).coerceAtLeast(1.5f)
        val path = Path()
        val steps = 120
        for (i in 0..steps) {
            val fraction = i / steps.toFloat()
            val x = size.width * fraction
            val angle = phase + fraction * 4f * PI.toFloat()
            val y = midY + sin(angle) * amplitudePx
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = WaveColor, style = Stroke(width = 2.dp.toPx()))
    }
}
