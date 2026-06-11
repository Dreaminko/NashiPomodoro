package com.example.nashitimer.core.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerStateTest {
    @Test
    fun remainingFraction_decreasesWithRemainingTime() {
        val full = TimerState(remainingMs = 60_000, totalMs = 60_000)
        val half = TimerState(remainingMs = 30_000, totalMs = 60_000)
        val empty = TimerState(remainingMs = 0, totalMs = 60_000)

        assertEquals(1f, full.remainingFraction, 0f)
        assertEquals(0.5f, half.remainingFraction, 0f)
        assertEquals(0f, empty.remainingFraction, 0f)
    }

    @Test
    fun remainingFraction_isClampedToValidRange() {
        val overfull = TimerState(remainingMs = 90_000, totalMs = 60_000)
        val invalidTotal = TimerState(remainingMs = 30_000, totalMs = 0)

        assertEquals(1f, overfull.remainingFraction, 0f)
        assertEquals(0f, invalidTotal.remainingFraction, 0f)
    }
}
