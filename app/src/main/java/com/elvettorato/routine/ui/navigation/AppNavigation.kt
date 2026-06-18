package com.elvettorato.routine.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elvettorato.routine.ui.screens.EditorScreen
import com.elvettorato.routine.ui.screens.EditorViewModel
import com.elvettorato.routine.ui.screens.HomeScreen
import com.elvettorato.routine.ui.screens.HomeViewModel
import com.elvettorato.routine.ui.screens.SettingsScreen

private const val ANIM_DURATION = 300

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable(
            route = "home",
            enterTransition = { fadeIn(tween(ANIM_DURATION)) },
            exitTransition = { fadeOut(tween(ANIM_DURATION)) },
            popEnterTransition = { fadeIn(tween(ANIM_DURATION)) },
            popExitTransition = { fadeOut(tween(ANIM_DURATION)) }
        ) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = viewModel,
                onAddRoutine = { navController.navigate("editor") },
                onEditRoutine = { id -> navController.navigate("editor/$id") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable(
            route = "settings",
            enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } + fadeIn(tween(ANIM_DURATION)) },
            exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } + fadeOut(tween(ANIM_DURATION)) },
            popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } + fadeIn(tween(ANIM_DURATION)) },
            popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { -it } + fadeOut(tween(ANIM_DURATION)) }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "editor/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.LongType }),
            enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } + fadeIn(tween(ANIM_DURATION)) },
            exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } + fadeOut(tween(ANIM_DURATION)) },
            popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } + fadeIn(tween(ANIM_DURATION)) },
            popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { -it } + fadeOut(tween(ANIM_DURATION)) }
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId")
            val viewModel: EditorViewModel = viewModel()
            EditorScreen(
                viewModel = viewModel,
                routineId = routineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "editor",
            enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } + fadeIn(tween(ANIM_DURATION)) },
            exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } + fadeOut(tween(ANIM_DURATION)) },
            popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } + fadeIn(tween(ANIM_DURATION)) },
            popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { -it } + fadeOut(tween(ANIM_DURATION)) }
        ) {
            val viewModel: EditorViewModel = viewModel()
            EditorScreen(
                viewModel = viewModel,
                routineId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
