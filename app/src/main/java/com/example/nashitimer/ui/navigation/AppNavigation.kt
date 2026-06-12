package com.example.nashitimer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nashitimer.ui.settings.AppearanceSettingsScreen
import com.example.nashitimer.ui.settings.DebugScreen
import com.example.nashitimer.ui.settings.ReminderSettingsScreen
import com.example.nashitimer.ui.settings.SettingsScreen
import com.example.nashitimer.ui.settings.TimerSettingsScreen
import com.example.nashitimer.ui.stats.StatsScreen
import com.example.nashitimer.ui.tasks.TaskListScreen
import com.example.nashitimer.ui.timer.TimerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = destination?.route !in setOf(
        AppRoute.SETTINGS.route,
        AppRoute.SETTINGS_TIMER.route,
        AppRoute.SETTINGS_REMINDER.route,
        AppRoute.SETTINGS_APPEARANCE.route,
        AppRoute.DEBUG.route
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) BottomNavBar(navController, destination)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.TIMER.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoute.TIMER.route) {
                TimerScreen(onOpenSettings = { navController.navigate(AppRoute.SETTINGS.route) })
            }
            composable(AppRoute.STATS.route) { StatsScreen() }
            composable(AppRoute.TASKS.route) { TaskListScreen() }
            composable(AppRoute.SETTINGS.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTimer = { navController.navigate(AppRoute.SETTINGS_TIMER.route) },
                    onOpenReminder = {
                        navController.navigate(AppRoute.SETTINGS_REMINDER.route)
                    },
                    onOpenAppearance = {
                        navController.navigate(AppRoute.SETTINGS_APPEARANCE.route)
                    },
                    onOpenDebug = { navController.navigate(AppRoute.DEBUG.route) }
                )
            }
            composable(AppRoute.SETTINGS_TIMER.route) {
                TimerSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoute.SETTINGS_REMINDER.route) {
                ReminderSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoute.SETTINGS_APPEARANCE.route) {
                AppearanceSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoute.DEBUG.route) {
                DebugScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
