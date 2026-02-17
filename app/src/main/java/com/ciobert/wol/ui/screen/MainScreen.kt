package com.ciobert.wol.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddPcClick: () -> Unit,
    onPcClick: (String) -> Unit,
    onScheduleClick: (String) -> Unit,
    onShowAllSchedules: () -> Unit
) {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Devices,
        Screen.Settings
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = screen.icon, 
                                contentDescription = null
                            ) 
                        },
                        label = { 
                            Text(
                                text = screen.label
                            ) 
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Enable state restoration
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (MaterialTheme.colorScheme.primary.luminance() > 0.5f) Color.Black else Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = if (MaterialTheme.colorScheme.primary.luminance() < 0.1f) 
                                Color.White.copy(alpha = 0.12f) // Fallback for very dark/black colors
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            unselectedIconColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                            unselectedTextColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToDevices = { 
                     // Switch tab programmatically
                     navController.navigate(Screen.Devices.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                     }
                    },
                    onAddPcClick = onAddPcClick,
                    onShowAllSchedules = onShowAllSchedules
                )
            }
            composable(Screen.Devices.route) {
                DashboardScreen(
                    onAddPcClick = onAddPcClick,
                    onPcClick = onPcClick,
                    onScheduleClick = onScheduleClick
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAppearance = { navController.navigate("settings_appearance") },
                    onNavigateToData = { navController.navigate("settings_data") },
                    onNavigateToStability = { navController.navigate("settings_stability") }
                )
            }
            composable("settings_appearance") {
                AppearanceSettingsScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable("settings_data") {
                DataManagementSettingsScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable("settings_stability") {
                StabilitySettingsScreen(onNavigateBack = { navController.navigateUp() })
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Devices : Screen("devices", "Devices", Icons.Default.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
