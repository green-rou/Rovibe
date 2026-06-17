package com.greenrou.rovibe.ui.screen.composition_editor.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.ui.theme.TerminalSubtext
import com.greenrou.rovibe.ui.theme.TerminalSurface

@Composable
fun BeatRuler(
    totalBars: Int,
    barWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.height(RulerHeight).background(TerminalSurface)) {
        repeat(totalBars) { bar ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "${bar + 1}",
                    color = TerminalSubtext,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(barWidth),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(TerminalSubtext.copy(alpha = 0.3f)),
                )
            }
        }
    }
}

val RulerHeight = 28.dp
val TrackHeight = 56.dp
val BarWidth = 64.dp
val HeaderWidth = 104.dp
