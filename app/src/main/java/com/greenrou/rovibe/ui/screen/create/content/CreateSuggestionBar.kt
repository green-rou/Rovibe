package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.data.sound.SoundCommandSpec

private val BarBackground = Color(0xFF161B22)
private val ChipBackground = Color(0xFF21262D)
private val HintChipBackground = Color(0xFF1C2333)
private val UsageColor = Color(0xFF58F0A0)
private val HintUsageColor = Color(0xFF79C0FF)
private val DescriptionColor = Color(0xFF8B949E)

@Composable
fun CreateSuggestionBar(
    suggestions: List<SoundCommandSpec>,
    parameterHint: SoundCommandSpec?,
    onSuggestionClick: (SoundCommandSpec) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty() && parameterHint == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BarBackground)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (suggestions.isNotEmpty()) {
            suggestions.forEach { spec ->
                SuggestionChip(spec = spec, onClick = { onSuggestionClick(spec) })
            }
        } else if (parameterHint != null) {
            HintChip(spec = parameterHint)
        }
    }
}

@Composable
private fun SuggestionChip(spec: SoundCommandSpec, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = spec.usage,
            color = CommandColors[spec.name] ?: UsageColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        Text(
            text = stringResource(spec.descriptionRes),
            color = DescriptionColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun HintChip(spec: SoundCommandSpec) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HintChipBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = spec.usage,
            color = HintUsageColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        Text(
            text = stringResource(spec.descriptionRes),
            color = DescriptionColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
        )
    }
}
