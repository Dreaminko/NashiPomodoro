package com.example.nashitimer.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsStepTest {
    @Test
    fun snapToStep_usesFiveMinuteIntervals() {
        assertEquals(5, 6.snapToStep(5, 90, 5))
        assertEquals(10, 8.snapToStep(5, 90, 5))
        assertEquals(90, 89.snapToStep(5, 90, 5))
    }

    @Test
    fun snapToStep_clampsToRange() {
        assertEquals(5, 1.snapToStep(5, 15, 5))
        assertEquals(15, 30.snapToStep(5, 15, 5))
    }
}
