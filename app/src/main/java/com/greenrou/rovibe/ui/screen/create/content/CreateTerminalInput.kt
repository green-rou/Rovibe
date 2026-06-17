package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.greenrou.rovibe.data.sound.SoundCommandSpecs
import com.greenrou.rovibe.ui.screen.create.PianoVisualizer

private val TerminalBackground = Color(0xFF0D1117)
private val TerminalForeground = Color(0xFF58F0A0)
private val PromptColor = Color(0xFF6E7681)
private val CommentColor = Color(0xFF484F58)
private val SoloColor = Color(0xFFFFCA28)
private const val PROMPT = "$ "
private val CommandNameRegex = Regex("[A-Za-z_]+(?=\\()")
private val WaveLineRegex = Regex("""(?i)\bwave\s*\(\s*\)""")
private val BarsLineRegex = Regex("""(?i)\bbars\s*\(\s*\)""")
private val PianoLineRegex = Regex("""(?i)\bpiano\s*\([^)]*\)""")
private val TerminalPadding = 16.dp
private const val LINE_HEIGHT_SP = 20
private const val VISUALIZER_EXTRA_LINES = 4
private val LineHeight = LINE_HEIGHT_SP.sp

@Composable
fun CreateTerminalInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    amplitude: Float = 0f,
    spectrumBands: FloatArray = FloatArray(24),
    waveAnchorOffsets: List<Int> = emptyList(),
    barsAnchorOffsets: List<Int> = emptyList(),
    pianoVisualizers: List<PianoVisualizer> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val textStyle = TextStyle(
        color = TerminalForeground,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = LineHeight,
    )
    val verticalScroll = rememberScrollState()
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    val isEmpty = value.text.isEmpty()

    Column(modifier = modifier.background(TerminalBackground)) {
        Box(
            modifier = if (isEmpty) Modifier.fillMaxWidth()
            else Modifier.weight(1f).fillMaxWidth().clipToBounds()
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = textStyle,
                cursorBrush = SolidColor(TerminalForeground),
                visualTransformation = PromptTransformation,
                onTextLayout = { textLayout = it },
                modifier = if (isEmpty) {
                    Modifier
                        .fillMaxWidth()
                        .padding(TerminalPadding)
                } else {
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                        .horizontalScroll(rememberScrollState())
                        .padding(TerminalPadding)
                },
            )

            if (!isEmpty) {
                val layout = textLayout
                if (layout != null) {
                    val allAnchorOffsets = (waveAnchorOffsets + barsAnchorOffsets + pianoVisualizers.map { it.anchorOffset }).sorted()
                    waveAnchorOffsets.forEach { anchor ->
                        VisualizerSlot(anchor, value, layout, density, verticalScroll, allAnchorOffsets) { m ->
                            WaveVisualizer(amplitude = amplitude, modifier = m)
                        }
                    }
                    barsAnchorOffsets.forEach { anchor ->
                        VisualizerSlot(anchor, value, layout, density, verticalScroll, allAnchorOffsets) { m ->
                            BarsVisualizer(bands = spectrumBands, modifier = m)
                        }
                    }
                    pianoVisualizers.forEach { piano ->
                        VisualizerSlot(piano.anchorOffset, value, layout, density, verticalScroll, allAnchorOffsets) { m ->
                            PianoRollVisualizer(notes = piano.notes, amplitude = amplitude, tempo = piano.tempo, modifier = m)
                        }
                    }
                }
            }
        }

        if (isEmpty) {
            Spacer(modifier = Modifier.height(with(LocalDensity.current) { LineHeight.toDp() }))
            CommandCheatSheet(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun VisualizerSlot(
    anchorOffset: Int,
    value: TextFieldValue,
    layout: TextLayoutResult,
    density: Density,
    verticalScroll: ScrollState,
    allAnchorOffsets: List<Int>,
    content: @Composable (Modifier) -> Unit,
) {
    val transformedEnd = transformedOffset(value.text, anchorOffset, allAnchorOffsets)
        .coerceIn(0, layout.layoutInput.text.length)
    val lineIndex = layout.getLineForOffset(transformedEnd)
        .coerceIn(0, (layout.lineCount - 1).coerceAtLeast(0))
    val lineBottomPx = layout.getLineBottom(lineIndex)
    val lineHeightPx = with(density) { LineHeight.toPx() }
    val paddingPx = with(density) { TerminalPadding.toPx() }
    val gapPx = lineHeightPx
    val contentHeightPx = lineHeightPx * (VISUALIZER_EXTRA_LINES - 1)
    val offsetYPx = paddingPx + lineBottomPx + gapPx - verticalScroll.value

    content(
        Modifier
            .fillMaxWidth()
            .offset(y = with(density) { offsetYPx.toDp() })
            .height(with(density) { contentHeightPx.toDp() })
            .pointerInput(Unit) { detectTapGestures { } },
    )
}

@Composable
private fun CommandCheatSheet(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TerminalPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SoundCommandSpecs.ALL.forEach { spec ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = spec.name,
                    color = CommandColors[spec.name] ?: TerminalForeground,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.width(80.dp),
                )
                Text(
                    text = stringResource(spec.descriptionRes),
                    color = PromptColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

private fun transformedOffset(original: String, offset: Int, allAnchorOffsets: List<Int> = emptyList()): Int {
    val clamped = offset.coerceIn(0, original.length)
    val lineIndex = original.substring(0, clamped).count { it == '\n' }
    val visualizersAbove = allAnchorOffsets.count { it < clamped }
    return clamped + (lineIndex + 1) * PROMPT.length + visualizersAbove * VISUALIZER_EXTRA_LINES
}

private object PromptTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var prevExtended = false
        val extensionNewlinePositions = mutableSetOf<Int>()
        var origOffset = 0

        val transformed = buildAnnotatedString {
            text.text.split("\n").forEachIndexed { index, line ->
                if (index > 0) {
                    append("\n")
                    origOffset++
                    if (prevExtended) {
                        extensionNewlinePositions.add(origOffset - 1)
                        repeat(VISUALIZER_EXTRA_LINES) { append("\n") }
                    }
                }

                val isComment = line.trimStart().startsWith("#")
                val isSolo = !isComment && line.trimStart().startsWith("!")
                val renderLine: AnnotatedString.Builder.() -> Unit = {
                    when {
                        isComment -> withStyle(SpanStyle(color = CommentColor)) { append(PROMPT + line) }
                        isSolo -> {
                            withStyle(SpanStyle(color = PromptColor)) { append(PROMPT) }
                            val bangIndex = line.indexOf('!')
                            if (bangIndex > 0) append(line.substring(0, bangIndex))
                            withStyle(SpanStyle(color = SoloColor)) { append("!") }
                            appendHighlightedLine(line.substring(bangIndex + 1))
                        }
                        else -> {
                            withStyle(SpanStyle(color = PromptColor)) { append(PROMPT) }
                            appendHighlightedLine(line)
                        }
                    }
                }

                var extend = false
                if (!isComment && WaveLineRegex.containsMatchIn(line)) extend = true
                if (!isComment && BarsLineRegex.containsMatchIn(line)) extend = true
                if (!isComment && PianoLineRegex.containsMatchIn(line)) extend = true

                renderLine()
                origOffset += line.length
                prevExtended = extend
            }
            if (prevExtended) {
                extensionNewlinePositions.add(origOffset)
                repeat(VISUALIZER_EXTRA_LINES) { append("\n") }
            }
        }

        return TransformedText(
            transformed,
            VisualizerOffsetMapping(text.text, extensionNewlinePositions),
        )
    }
}

private fun AnnotatedString.Builder.appendHighlightedLine(line: String) {
    var lastEnd = 0
    for (match in CommandNameRegex.findAll(line)) {
        if (match.range.first < lastEnd) continue
        if (match.range.first > lastEnd) {
            append(line.substring(lastEnd, match.range.first))
        }
        val name = match.value
        val color = CommandColors[name.lowercase()] ?: TerminalForeground
        val openParen = match.range.last + 1
        val closeParen = findMatchingParen(line, openParen)
        val colorEnd = if (closeParen >= 0) closeParen + 1 else line.length
        withStyle(SpanStyle(color = color)) {
            append(line.substring(match.range.first, colorEnd))
        }
        lastEnd = colorEnd
    }
    if (lastEnd < line.length) {
        append(line.substring(lastEnd))
    }
}

private fun findMatchingParen(line: String, openIndex: Int): Int {
    var depth = 1
    var i = openIndex + 1
    while (i < line.length) {
        when (line[i]) {
            '(' -> depth++
            ')' -> { depth--; if (depth == 0) return i }
        }
        i++
    }
    return -1
}

private class VisualizerOffsetMapping(
    private val original: String,
    extensionNewlinePositions: Set<Int>,
) : OffsetMapping {

    private val extraBefore = IntArray(original.length + 1)

    init {
        var extra = PROMPT.length
        for (i in 0..original.length) {
            extraBefore[i] = extra
            if (i < original.length && original[i] == '\n') {
                if (i in extensionNewlinePositions) extra += VISUALIZER_EXTRA_LINES
                extra += PROMPT.length
            }
        }
    }

    override fun originalToTransformed(offset: Int): Int {
        val clamped = offset.coerceIn(0, original.length)
        return clamped + extraBefore[clamped]
    }

    override fun transformedToOriginal(offset: Int): Int {
        var lo = 0
        var hi = original.length
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (originalToTransformed(mid) <= offset) lo = mid else hi = mid - 1
        }
        return lo
    }
}
