package com.example.nashitimer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {
    @Test
    fun focusDurationMs_usesNormalMinutesWhenDebugModeIsOff() {
        val settings = AppSettings(
            focusDurationMin = 25,
            debugModeEnabled = false,
            debugFocusDurationSec = 3
        )

        assertEquals(25 * 60_000L, settings.focusDurationMs)
    }

    @Test
    fun focusDurationMs_usesDebugSecondsWhenDebugModeIsOn() {
        val settings = AppSettings(
            debugModeEnabled = true,
            debugFocusDurationSec = 3
        )

        assertEquals(3_000L, settings.focusDurationMs)
    }

    @Test
    fun focusDurationMs_clampsDebugDuration() {
        assertEquals(
            1_000L,
            AppSettings(debugModeEnabled = true, debugFocusDurationSec = 0).focusDurationMs
        )
        assertEquals(
            3_600_000L,
            AppSettings(debugModeEnabled = true, debugFocusDurationSec = 9_999).focusDurationMs
        )
    }

    @Test
    fun vibrationAmplitude_mapsPercentageToAndroidRange() {
        assertEquals(26, AppSettings(vibrationIntensity = 10).vibrationAmplitude)
        assertEquals(153, AppSettings(vibrationIntensity = 60).vibrationAmplitude)
        assertEquals(255, AppSettings(vibrationIntensity = 100).vibrationAmplitude)
    }

    @Test
    fun vibrationAmplitude_clampsStoredIntensity() {
        assertEquals(26, AppSettings(vibrationIntensity = -1).vibrationAmplitude)
        assertEquals(255, AppSettings(vibrationIntensity = 999).vibrationAmplitude)
    }

    @Test
    fun normalized_clampsEveryPersistedNumericSetting() {
        val normalized = AppSettings(
            focusDurationMin = -1,
            shortBreakMin = 0,
            longBreakMin = 999,
            longBreakInterval = 0,
            dailyGoal = 999,
            vibrationIntensity = 0,
            debugFocusDurationSec = 99_999
        ).normalized()

        assertEquals(5, normalized.focusDurationMin)
        assertEquals(5, normalized.shortBreakMin)
        assertEquals(30, normalized.longBreakMin)
        assertEquals(2, normalized.longBreakInterval)
        assertEquals(20, normalized.dailyGoal)
        assertEquals(10, normalized.vibrationIntensity)
        assertEquals(3_600, normalized.debugFocusDurationSec)
    }
}
