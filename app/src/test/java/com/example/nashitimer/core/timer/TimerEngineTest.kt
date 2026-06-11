package com.example.nashitimer.core.timer

import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
        val scope = CoroutineScope(Job())

        engine.start(scope, settings)
        engine.skip(scope, settings)

        assertEquals(TimerPhase.SHORT_BREAK, engine.state.value.phase)
        assertEquals(0, engine.state.value.completedFocusRounds)
        assertTrue(engine.state.value.isRunning)
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun skipBreak_movesToFocusAndKeepsPausedState() {
        val engine = TimerEngine()
        val scope = CoroutineScope(Job())

        engine.start(scope, settings)
        engine.skip(scope, settings)
        engine.pause()
        engine.skip(scope, settings)

        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertFalse(engine.state.value.isRunning)
        scope.coroutineContext[Job]?.cancel()
    }
}
