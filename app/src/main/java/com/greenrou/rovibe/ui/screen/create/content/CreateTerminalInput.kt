package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TerminalBackground = Color(0xFF0D1117)
private val TerminalForeground = Color(0xFF58F0A0)
private val PromptColor = Color(0xFF6E7681)
private const val PROMPT = "$ "
private val CommandNameRegex = Regex("[A-Za-z]+(?=\\()")
private val TerminalPadding = 16.dp
private val LineHeight = 20.sp

@Composable
fun CreateTerminalInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    amplitude: Float = 0f,
    waveAnchorOffset: Int? = null,
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

    Box(modifier = modifier.background(TerminalBackground)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            cursorBrush = SolidColor(TerminalForeground),
            visualTransformation = PromptTransformation,
            onTextLayout = { textLayout = it },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
                .horizontalScroll(rememberScrollState())
                .padding(TerminalPadding),
        )

        val layout = textLayout
        if (waveAnchorOffset != null && layout != null) {
            val transformedEnd = transformedOffset(value.text, waveAnchorOffset)
                .coerceIn(0, layout.layoutInput.text.length)
            val lineIndex = layout.getLineForOffset(transformedEnd)
                .coerceIn(0, (layout.lineCount - 1).coerceAtLeast(0))
            val lineBottomPx = layout.getLineBottom(lineIndex)
            val lineHeightPx = with(density) { LineHeight.toPx() }
            val paddingPx = with(density) { TerminalPadding.toPx() }
            val offsetYPx = paddingPx + lineBottomPx - verticalScroll.value

            WaveVisualizer(
                amplitude = amplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = with(density) { offsetYPx.toDp() })
                    .height(with(density) { (lineHeightPx * 3).toDp() }),
            )
        }
    }
}

private fun transformedOffset(original: String, offset: Int): Int {
    val clamped = offset.coerceIn(0, original.length)
    val lineIndex = original.substring(0, clamped).count { it == '\n' }
    return clamped + (lineIndex + 1) * PROMPT.length
}

private object PromptTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = buildAnnotatedString {
            text.text.split("\n").forEachIndexed { index, line ->
                if (index > 0) append("\n")
                withStyle(SpanStyle(color = PromptColor)) { append(PROMPT) }
                appendHighlightedLine(line)
            }
        }
        return TransformedText(transformed, PromptOffsetMapping(text.text, transformed.text))
    }
}

private fun AnnotatedString.Builder.appendHighlightedLine(line: String) {
    var lastEnd = 0
    for (match in CommandNameRegex.findAll(line)) {
        if (match.range.first > lastEnd) {
            append(line.substring(lastEnd, match.range.first))
        }
        val name = match.value
        val color = CommandColors[name.lowercase()] ?: TerminalForeground
        withStyle(SpanStyle(color = color)) { append(name) }
        lastEnd = match.range.last + 1
    }
    if (lastEnd < line.length) {
        append(line.substring(lastEnd))
    }
}

private class PromptOffsetMapping(
    private val original: String,
    private val transformed: String,
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        val clamped = offset.coerceIn(0, original.length)
        val lineIndex = original.substring(0, clamped).count { it == '\n' }
        return clamped + (lineIndex + 1) * PROMPT.length
    }

    override fun transformedToOriginal(offset: Int): Int {
        val clamped = offset.coerceIn(0, transformed.length)
        val lineIndex = transformed.substring(0, clamped).count { it == '\n' }
        val lineStart = transformed.lastIndexOf('\n', clamped - 1) + 1
        val lineFloor = lineStart - lineIndex * PROMPT.length
        return (clamped - (lineIndex + 1) * PROMPT.length)
            .coerceAtLeast(lineFloor)
            .coerceAtMost(original.length)
    }
}
