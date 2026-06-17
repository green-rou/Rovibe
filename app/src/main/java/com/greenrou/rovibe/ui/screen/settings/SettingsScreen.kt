package com.greenrou.rovibe.ui.screen.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.greenrou.rovibe.R
import com.greenrou.rovibe.data.sound.SoundCommandSpecs

private val ScreenBackground = Color(0xFF0D1117)
private val SectionBackground = Color(0xFF161B22)
private val SectionHeaderColor = Color(0xFF6E7681)
private val PrimaryTextColor = Color(0xFFE6EDF3)
private val SecondaryTextColor = Color(0xFF8B949E)
private val AccentColor = Color(0xFF58F0A0)
private val DividerColor = Color(0xFF21262D)

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val initialLang = remember {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) "en" else locales[0]?.language ?: "en"
    }
    var selectedLang by remember { mutableStateOf(initialLang) }

    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SettingsSection(title = stringResource(R.string.settings_language)) {
            LanguageRow(
                label = stringResource(R.string.lang_english),
                selected = selectedLang == "en",
                onClick = {
                    selectedLang = "en"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                },
            )
            SectionDivider()
            LanguageRow(
                label = stringResource(R.string.lang_ukrainian),
                selected = selectedLang == "uk",
                onClick = {
                    selectedLang = "uk"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("uk"))
                },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_commands)) {
            SoundCommandSpecs.ALL.forEachIndexed { index, spec ->
                if (index > 0) SectionDivider()
                CommandRow(name = spec.name, description = stringResource(spec.descriptionRes))
            }
        }

        SettingsSection(title = stringResource(R.string.settings_about)) {
            InfoRow(text = "Rovibe v$version")
            SectionDivider()
            InfoRow(text = stringResource(R.string.about_description))
            SectionDivider()
            LinkRow(
                text = stringResource(R.string.settings_github),
                onClick = { uriHandler.openUri("https://github.com/green-rou/Rovibe") },
            )
            SectionDivider()
            LinkRow(
                text = stringResource(R.string.settings_support),
                onClick = { uriHandler.openUri("https://ko-fi.com/C0C31ZLH6K") },
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            color = SectionHeaderColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SectionBackground),
        ) {
            content()
        }
    }
}

@Composable
private fun LanguageRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentColor,
                unselectedColor = SecondaryTextColor,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (selected) AccentColor else PrimaryTextColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun InfoRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            color = SecondaryTextColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            color = AccentColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun CommandRow(name: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = AccentColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = description,
            color = SecondaryTextColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = DividerColor,
        thickness = 0.5.dp,
    )
}
