package com.greenrou.rovibe.ui.screen.composition_editor.content

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.composition.CompositionTrack
import com.greenrou.rovibe.data.composition.PatternBlock
import com.greenrou.rovibe.ui.theme.TerminalBg
import com.greenrou.rovibe.ui.theme.TerminalSubtext
import kotlin.math.roundToInt

@Composable
fun TrackRow(
    track: CompositionTrack,
    totalBars: Int,
    sounds: List<SoundItem>,
    onCellTap: (bar: Float) -> Unit,
    onPatternClick: (PatternBlock) -> Unit,
    onMovePattern: (patternId: String, newStartBar: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridColor = TerminalSubtext.copy(alpha = 0.15f)

    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragDeltaPx by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .width(BarWidth * totalBars)
            .height(TrackHeight)
            .drawBehind {
                drawRect(TerminalBg)
                for (bar in 1..totalBars) {
                    val x = bar * BarWidth.toPx()
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                    )
                }
            }
            .pointerInput(track.id) {
                detectTapGestures { offset ->
                    onCellTap(offset.x / BarWidth.toPx())
                }
            },
    ) {
        track.patterns.forEach { block ->
            val soundName = sounds.find { it.id == block.soundId }?.name ?: "?"
            val isDragging = draggingId == block.id
            Box(
                modifier = Modifier
                    .offset(x = BarWidth * block.startBar)
                    .offset { IntOffset(if (isDragging) dragDeltaPx.roundToInt() else 0, 0) }
                    .width(BarWidth * block.durationBars)
                    .fillMaxHeight()
                    .pointerInput(block.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingId = block.id
                                dragDeltaPx = 0f
                            },
                            onDrag = { _, dragAmount ->
                                dragDeltaPx += dragAmount.x
                            },
                            onDragEnd = {
                                val newStart = (block.startBar + dragDeltaPx / BarWidth.toPx())
                                    .coerceAtLeast(0f)
                                onMovePattern(block.id, newStart)
                                draggingId = null
                                dragDeltaPx = 0f
                            },
                            onDragCancel = {
                                draggingId = null
                                dragDeltaPx = 0f
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                PatternBlockItem(
                    soundName = soundName,
                    onClick = { onPatternClick(block) },
                )
            }
        }
    }
}
