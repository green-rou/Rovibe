package com.greenrou.rovibe.ui.screen.composition_editor.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.R
import com.greenrou.rovibe.data.SoundItem
import com.greenrou.rovibe.data.composition.Composition
import com.greenrou.rovibe.data.composition.PatternBlock
import com.greenrou.rovibe.ui.screen.composition_editor.CompositionPlaybackState
import com.greenrou.rovibe.ui.theme.TerminalBg
import com.greenrou.rovibe.ui.theme.TerminalSubtext
import com.greenrou.rovibe.ui.theme.TerminalSurface

private val PlaybackLineColor = Color(0xFFFF4040).copy(alpha = 0.80f)

@Composable
fun CompositionGrid(
    composition: Composition,
    sounds: List<SoundItem>,
    playbackState: CompositionPlaybackState,
    playbackPositionBar: Float,
    onCellTap: (trackId: String, bar: Float) -> Unit,
    onPatternClick: (trackId: String, pattern: PatternBlock) -> Unit,
    onMovePattern: (trackId: String, patternId: String, newStartBar: Float) -> Unit,
    onAddTrack: () -> Unit,
    onDeleteTrack: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vertScroll = rememberScrollState()
    val horizScroll = rememberScrollState()

    val totalBars = maxOf(
        composition.tracks.flatMap { it.patterns }.maxOfOrNull { it.startBar + it.durationBars } ?: 0f,
        16f,
    ).toInt() + 8

    Box(modifier = modifier.fillMaxSize()) {
        // ── Right scrollable content ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(start = HeaderWidth)
                .fillMaxSize()
                .horizontalScroll(horizScroll),
        ) {
            Column(modifier = Modifier.verticalScroll(vertScroll)) {
                BeatRuler(totalBars = totalBars, barWidth = BarWidth)

                composition.tracks.forEachIndexed { index, track ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(BarWidth * totalBars)
                                .height(1.dp)
                                .background(TerminalSurface),
                        )
                    }
                    TrackRow(
                        track = track,
                        totalBars = totalBars,
                        sounds = sounds,
                        onCellTap = { bar -> onCellTap(track.id, bar) },
                        onPatternClick = { pattern -> onPatternClick(track.id, pattern) },
                        onMovePattern = { patternId, newStart -> onMovePattern(track.id, patternId, newStart) },
                    )
                }

                // Spacer matching the add-track button height in the left header
                Box(
                    modifier = Modifier
                        .width(BarWidth * totalBars)
                        .height(TrackHeight)
                        .background(TerminalBg),
                )
            }

            // Playback position line (overlaid in the scrollable content space)
            if (playbackState == CompositionPlaybackState.PLAYING) {
                Box(
                    modifier = Modifier
                        .offset(x = BarWidth * playbackPositionBar)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(PlaybackLineColor),
                )
            }
        }

        // ── Left fixed header column ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(HeaderWidth)
                .fillMaxHeight()
                .clipToBounds(),
        ) {
            Column(modifier = Modifier.verticalScroll(vertScroll, enabled = false)) {
                // Ruler spacer
                Box(
                    modifier = Modifier
                        .width(HeaderWidth)
                        .height(RulerHeight)
                        .background(TerminalSurface),
                )

                composition.tracks.forEachIndexed { index, track ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(HeaderWidth)
                                .height(1.dp)
                                .background(TerminalSurface),
                        )
                    }
                    TrackHeader(
                        track = track,
                        onDelete = { onDeleteTrack(track.id) },
                    )
                }

                // Add-track button — always visible, no horizontal scroll needed
                Box(
                    modifier = Modifier
                        .width(HeaderWidth)
                        .height(TrackHeight)
                        .background(TerminalBg)
                        .clickable(onClick = onAddTrack),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+ ${stringResource(R.string.add_track)}",
                        color = TerminalSubtext,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // ── Header/ruler separator line ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RulerHeight + 1.dp)
                .padding(top = RulerHeight)
                .background(TerminalSurface),
        )
    }
}
