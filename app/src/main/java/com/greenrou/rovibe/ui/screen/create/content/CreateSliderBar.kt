package com.greenrou.rovibe.ui.screen.create.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.R

private val BarBackground = Color(0xFF161B22)
private val ValueColor = Color(0xFF58A6FF)

@Composable
fun CreateSliderBar(
    position: Float,
    value: String,
    onPositionChange: (Float) -> Unit,
    onPositionChangeFinished: () -> Unit = {},
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BarBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slider(
            value = position,
            onValueChange = onPositionChange,
            onValueChangeFinished = onPositionChangeFinished,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = ValueColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier
                .width(48.dp)
                .padding(start = 8.dp),
        )
        IconButton(onClick = onDone) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.done),
            )
        }
    }
}
