package com.greenrou.rovibe.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.greenrou.rovibe.ui.screen.compositions.CompositionsScreen
import com.greenrou.rovibe.ui.screen.create.content.CreateScreen
import com.greenrou.rovibe.ui.screen.home.content.HomeScreen
import com.greenrou.rovibe.ui.screen.settings.SettingsScreen

@Composable
fun RovibeNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute?.startsWith(Routes.CREATE) != true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.SOUNDS) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SOUNDS,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.SOUNDS) {
                HomeScreen(
                    onAddClick = { navController.navigate(Routes.create()) },
                    onItemClick = { itemId -> navController.navigate(Routes.create(itemId)) },
                )
            }
            composable(Routes.COMPOSITIONS) {
                CompositionsScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = Routes.CREATE_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ITEM_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                CreateScreen(
                    itemId = backStackEntry.arguments?.getString(Routes.ITEM_ID_ARG),
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
