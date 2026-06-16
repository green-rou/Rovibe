package com.greenrou.rovibe.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.greenrou.rovibe.R

private val BarBackground = Color(0xFF161B22)
private val SelectedColor = Color(0xFF58F0A0)
private val UnselectedColor = Color(0xFF6E7681)
private val IndicatorColor = Color(0xFF21262D)

private data class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem(Routes.SOUNDS, R.string.tab_sounds, Icons.Filled.MusicNote),
    BottomNavItem(Routes.COMPOSITIONS, R.string.tab_compositions, Icons.Filled.QueueMusic),
    BottomNavItem(Routes.SETTINGS, R.string.tab_settings, Icons.Filled.Settings),
)

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = BarBackground,
        tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SelectedColor,
                    selectedTextColor = SelectedColor,
                    indicatorColor = IndicatorColor,
                    unselectedIconColor = UnselectedColor,
                    unselectedTextColor = UnselectedColor,
                ),
            )
        }
    }
}
