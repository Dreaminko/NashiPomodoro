package com.example.nashitimer.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController

enum class AppRoute(val route: String, val label: String) {
    TIMER("timer", "Timer"),
    STATS("stats", "Stats"),
    TASKS("tasks", "Tasks"),
    SETTINGS("settings", "Settings")
}

@Composable
fun BottomNavBar(navController: NavHostController, destination: NavDestination?) {
    NavigationBar {
        listOf(AppRoute.TIMER, AppRoute.STATS, AppRoute.TASKS).forEach { item ->
            NavigationBarItem(
                selected = destination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(AppRoute.TIMER.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(item.label.take(1)) },
                label = { Text(item.label) }
            )
        }
    }
}
