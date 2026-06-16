package com.greenrou.rovibe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TerminalColorScheme = darkColorScheme(
    background   = TerminalBg,
    surface      = TerminalSurface,
    primary      = TerminalGreen,
    onPrimary    = TerminalOnGreen,
    onBackground = TerminalText,
    onSurface    = TerminalText,
)

@Composable
fun RovibeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TerminalColorScheme,
        typography  = Typography,
        content     = content,
    )
}
