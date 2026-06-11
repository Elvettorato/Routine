package com.elvettorato.routine.ui.navigation

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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = viewModel,
                onAddRoutine = { navController.navigate("editor") },
                onEditRoutine = { id -> navController.navigate("editor/$id") }
            )
        }
        composable(
            route = "editor/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId")
            val viewModel: EditorViewModel = viewModel()
            EditorScreen(
                viewModel = viewModel,
                routineId = routineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("editor") {
            val viewModel: EditorViewModel = viewModel()
            EditorScreen(
                viewModel = viewModel,
                routineId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
