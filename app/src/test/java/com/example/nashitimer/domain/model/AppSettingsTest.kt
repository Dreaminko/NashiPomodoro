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
}
