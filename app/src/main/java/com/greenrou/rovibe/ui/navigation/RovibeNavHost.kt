package com.greenrou.rovibe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.greenrou.rovibe.ui.screen.create.content.CreateScreen
import com.greenrou.rovibe.ui.screen.home.content.HomeScreen

@Composable
fun RovibeNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
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
}
