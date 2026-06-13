package com.example.nashitimer.core.timer

import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerEngineTest {
    private val settings = AppSettings(
        focusDurationMin = 25,
        shortBreakMin = 5,
        longBreakMin = 15,
        longBreakInterval = 4
    )

    @Test
    fun skipFocus_movesToBreakWithoutCompletingRound() {
        val engine = TimerEngine()

        engine.start(settings)
        engine.skip(settings)

        assertEquals(TimerPhase.SHORT_BREAK, engine.state.value.phase)
        assertEquals(0, engine.state.value.completedFocusRounds)
        assertTrue(engine.state.value.isRunning)
        engine.stop(settings)
    }

    @Test
    fun skipBreak_movesToFocusAndKeepsPausedState() {
        val engine = TimerEngine()

        engine.start(settings)
        engine.skip(settings)
        engine.pause()
        engine.skip(settings)

        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertFalse(engine.state.value.isRunning)
        engine.stop(settings)
    }

    @Test
    fun stopWithDuration_resetsToIdleUsingProvidedDuration() {
        val engine = TimerEngine()

        engine.start(settings)
        engine.stop(30_000L)

        assertEquals(TimerPhase.IDLE, engine.state.value.phase)
        assertEquals(30_000L, engine.state.value.remainingMs)
        assertEquals(30_000L, engine.state.value.totalMs)
        assertFalse(engine.state.value.isRunning)
    }
}
