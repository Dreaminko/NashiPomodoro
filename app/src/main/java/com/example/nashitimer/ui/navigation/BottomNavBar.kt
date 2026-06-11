package com.example.nashitimer.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController

enum class AppRoute(val route: String, val label: String) {
    TIMER("timer", "Focus"),
    STATS("stats", "Insights"),
    TASKS("tasks", "Tasks"),
    SETTINGS("settings", "Settings"),
    DEBUG("debug", "Debug")
}

@Composable
fun BottomNavBar(navController: NavHostController, destination: NavDestination?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        listOf(AppRoute.TIMER, AppRoute.STATS, AppRoute.TASKS).forEach { item ->
            val selected = destination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(AppRoute.TIMER.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { NavGlyph(item, selected) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun NavGlyph(route: AppRoute, selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        when (route) {
            AppRoute.TIMER -> {
                drawCircle(color, radius = 8.dp.toPx(), style = stroke)
                drawLine(color, center, Offset(center.x, 7.dp.toPx()), stroke.width, StrokeCap.Round)
                drawLine(color, center, Offset(17.dp.toPx(), center.y), stroke.width, StrokeCap.Round)
            }
            AppRoute.STATS -> {
                drawLine(color, Offset(5.dp.toPx(), 19.dp.toPx()), Offset(5.dp.toPx(), 14.dp.toPx()), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(12.dp.toPx(), 19.dp.toPx()), Offset(12.dp.toPx(), 9.dp.toPx()), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(19.dp.toPx(), 19.dp.toPx()), Offset(19.dp.toPx(), 5.dp.toPx()), stroke.width, StrokeCap.Round)
            }
            AppRoute.TASKS -> {
                listOf(7f, 12f, 17f).forEach { y ->
                    drawCircle(color, radius = 1.dp.toPx(), center = Offset(5.dp.toPx(), y.dp.toPx()))
                    drawLine(color, Offset(9.dp.toPx(), y.dp.toPx()), Offset(20.dp.toPx(), y.dp.toPx()), stroke.width, StrokeCap.Round)
                }
            }
            AppRoute.SETTINGS, AppRoute.DEBUG -> Unit
        }
    }
}
