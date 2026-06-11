package com.example.nashitimer.domain.model

data class AppSettings(
    val focusDurationMin: Int = 25,
    val shortBreakMin: Int = 5,
    val longBreakMin: Int = 15,
    val longBreakInterval: Int = 4,
    val dailyGoal: Int = 8,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val vibrationEnabled: Boolean = true,
    val debugModeEnabled: Boolean = false,
    val debugFocusDurationSec: Int = 30
) {
    val focusDurationMs: Long
        get() = if (debugModeEnabled) {
            debugFocusDurationSec.coerceIn(1, 3_600) * 1_000L
        } else {
            focusDurationMin.coerceIn(5, 90) * 60_000L
        }
}

enum class ThemeMode { LIGHT, DARK, FOLLOW_SYSTEM }
