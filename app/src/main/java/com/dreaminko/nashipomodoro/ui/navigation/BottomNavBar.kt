package com.dreaminko.nashipomodoro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController

import com.dreaminko.nashipomodoro.R

enum class AppRoute(val route: String) {
    TIMER("timer"),
    STATS("stats"),
    TASKS("tasks"),
    SETTINGS("settings"),
    SETTINGS_TIMER("settings/timer"),
    SETTINGS_REMINDER("settings/reminder"),
    SETTINGS_APPEARANCE("settings/appearance"),
    SETTINGS_DATA("settings/data"),
    DEBUG("debug")
}

@Composable
fun BottomNavBar(navController: NavHostController, destination: NavDestination?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp
            ) {
                listOf(AppRoute.TASKS, AppRoute.TIMER, AppRoute.STATS).forEach { item ->
                    val selected =
                        destination?.hierarchy?.any { it.route == item.route } == true
                    val label = stringResource(item.labelRes)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(AppRoute.TIMER.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor =
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

private val AppRoute.labelRes: Int
    get() = when (this) {
        AppRoute.TIMER -> R.string.nav_focus
        AppRoute.STATS -> R.string.nav_insights
        AppRoute.TASKS -> R.string.nav_tasks
        AppRoute.SETTINGS -> R.string.settings_title
        AppRoute.SETTINGS_TIMER -> R.string.settings_timer_section
        AppRoute.SETTINGS_REMINDER -> R.string.settings_reminder_section
        AppRoute.SETTINGS_APPEARANCE -> R.string.settings_appearance_section
        AppRoute.SETTINGS_DATA -> R.string.settings_data_section
        AppRoute.DEBUG -> R.string.debug_title
    }

private val AppRoute.icon: ImageVector
    get() = when (this) {
        AppRoute.TIMER -> Icons.Rounded.Timer
        AppRoute.STATS -> Icons.Rounded.Insights
        AppRoute.TASKS -> Icons.Rounded.Checklist
        AppRoute.SETTINGS,
        AppRoute.SETTINGS_TIMER,
        AppRoute.SETTINGS_REMINDER,
        AppRoute.SETTINGS_APPEARANCE,
        AppRoute.SETTINGS_DATA,
        AppRoute.DEBUG -> Icons.Rounded.Timer
    }
