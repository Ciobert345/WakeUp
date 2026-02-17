package com.ciobert.wol.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ciobert.wol.ui.screen.AddEditPcScreen
import com.ciobert.wol.ui.screen.DashboardScreen
import com.ciobert.wol.ui.screen.ScheduleScreen

@Composable
fun WolNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController, 
        startDestination = "main",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable("main") {
            com.ciobert.wol.ui.screen.MainScreen(
                onAddPcClick = { navController.navigate("add_edit_pc/new") },
                onPcClick = { pcId -> navController.navigate("add_edit_pc/$pcId") },
                onScheduleClick = { pcId -> navController.navigate("schedule/$pcId") },
                onShowAllSchedules = { navController.navigate("all_schedules") }
            )
        }
        composable(
            route = "add_edit_pc/{pcId}",
            arguments = listOf(navArgument("pcId") { type = NavType.StringType })
        ) {
            AddEditPcScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = "schedule/{pcId}",
            arguments = listOf(navArgument("pcId") { type = NavType.StringType })
        ) {
            ScheduleScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable("all_schedules") {
            com.ciobert.wol.ui.screen.AllSchedulesScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
