package com.greenrou.rovibe.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.greenrou.rovibe.ui.screen.composition_editor.content.CompositionEditorScreen
import com.greenrou.rovibe.ui.screen.compositions.CompositionsScreen
import com.greenrou.rovibe.ui.screen.create.content.CreateScreen
import com.greenrou.rovibe.ui.screen.home.content.HomeScreen
import com.greenrou.rovibe.ui.screen.settings.SettingsScreen

@Composable
fun RovibeNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val parentRoute = navBackStackEntry?.destination?.parent?.route
    val showBottomBar = currentRoute?.startsWith(Routes.CREATE) != true &&
        currentRoute?.startsWith(Routes.COMPOSITION_EDITOR) != true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    parentRoute = parentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
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
            startDestination = Routes.SOUNDS_GRAPH,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            navigation(startDestination = Routes.SOUNDS, route = Routes.SOUNDS_GRAPH) {
                composable(Routes.SOUNDS) {
                    HomeScreen(
                        onAddClick = { navController.navigate(Routes.create()) },
                        onItemClick = { itemId -> navController.navigate(Routes.create(itemId)) },
                    )
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
            navigation(startDestination = Routes.COMPOSITIONS, route = Routes.COMPOSITIONS_GRAPH) {
                composable(Routes.COMPOSITIONS) {
                    CompositionsScreen(
                        onEditorOpen = { id -> navController.navigate(Routes.compositionEditor(id)) },
                    )
                }
                composable(
                    route = Routes.COMPOSITION_EDITOR_PATTERN,
                    arguments = listOf(
                        navArgument(Routes.COMP_ID_ARG) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { backStackEntry ->
                    CompositionEditorScreen(
                        compositionId = backStackEntry.arguments?.getString(Routes.COMP_ID_ARG),
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
