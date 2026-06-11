package com.example.nashitimer.domain.model

data class AppSettings(
    val focusDurationMin: Int = 25,
    val shortBreakMin: Int = 5,
    val longBreakMin: Int = 15,
    val longBreakInterval: Int = 4,
    val dailyGoal: Int = 8,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val vibrationEnabled: Boolean = true
)

enum class ThemeMode { LIGHT, DARK, FOLLOW_SYSTEM }
