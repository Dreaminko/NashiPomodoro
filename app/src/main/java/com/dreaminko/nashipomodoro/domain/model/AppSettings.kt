package com.dreaminko.nashipomodoro.domain.model

import kotlin.math.roundToInt

data class AppSettings(
    val focusDurationMin: Int = 25,
    val shortBreakMin: Int = 5,
    val longBreakMin: Int = 15,
    val longBreakInterval: Int = 4,
    val dailyGoal: Int = 8,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val vibrationEnabled: Boolean = true,
    val vibrationIntensity: Int = 60,
    val glyphProgressEnabled: Boolean = true,
    val glyphProgressChannel: GlyphChannel = GlyphChannel.AUTO,
    val glyphShortBreakProgressEnabled: Boolean = true,
    val glyphShortBreakProgressChannel: GlyphChannel = GlyphChannel.AUTO,
    val glyphLongBreakProgressEnabled: Boolean = true,
    val glyphLongBreakProgressChannel: GlyphChannel = GlyphChannel.AUTO,
    val glyphCompletionFlashEnabled: Boolean = true,
    val debugModeEnabled: Boolean = false,
    val debugFocusDurationSec: Int = 30
) {
    val focusDurationMs: Long
        get() = if (debugModeEnabled) {
            debugFocusDurationSec.coerceIn(1, 3_600) * 1_000L
        } else {
            focusDurationMin.coerceIn(5, 90) * 60_000L
        }

    val vibrationAmplitude: Int
        get() = (vibrationIntensity.coerceIn(10, 100) * 255 / 100f)
            .roundToInt()
            .coerceIn(1, 255)

    val shortBreakDurationMs: Long
        get() = shortBreakMin.coerceIn(5, 15) * 60_000L

    val longBreakDurationMs: Long
        get() = longBreakMin.coerceIn(10, 30) * 60_000L

    val safeLongBreakInterval: Int
        get() = longBreakInterval.coerceIn(2, 8)

    fun normalized(): AppSettings = copy(
        focusDurationMin = focusDurationMin.coerceIn(5, 90),
        shortBreakMin = shortBreakMin.coerceIn(5, 15),
        longBreakMin = longBreakMin.coerceIn(10, 30),
        longBreakInterval = safeLongBreakInterval,
        dailyGoal = dailyGoal.coerceIn(1, 20),
        vibrationIntensity = vibrationIntensity.coerceIn(10, 100),
        debugFocusDurationSec = debugFocusDurationSec.coerceIn(1, 3_600)
    )
}

enum class ThemeMode { LIGHT, DARK, FOLLOW_SYSTEM }

enum class GlyphChannel { AUTO, A, B, C, D, E }
