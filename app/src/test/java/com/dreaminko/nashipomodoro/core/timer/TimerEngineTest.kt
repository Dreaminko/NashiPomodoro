package com.dreaminko.nashipomodoro.core.timer

import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
    fun skippedFocus_neverAwardsLongBreak() {
        val engine = TimerEngine()
        engine.restore(
            TimerSnapshot(
                phase = TimerPhase.FOCUS,
                activePhase = TimerPhase.FOCUS,
                remainingMs = settings.focusDurationMs,
                totalMs = settings.focusDurationMs,
                completedFocusRounds = 3,
                isRunning = false,
                isFaceDown = false,
                deadlineEpochMs = 0L
            ),
            settings
        )

        engine.skip(settings)

        assertEquals(TimerPhase.SHORT_BREAK, engine.state.value.phase)
        assertEquals(3, engine.state.value.completedFocusRounds)
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

    @Test
    fun focusCompletion_emitsExactlyOnceWithAccurateTimes() = runBlocking {
        var elapsedNanos = 0L
        var wallTime = 1_000_000L
        val engine = TimerEngine(
            nanoTime = { elapsedNanos },
            currentTimeMillis = { wallTime }
        )
        val debugSettings = settings.copy(
            debugModeEnabled = true,
            debugFocusDurationSec = 1
        )
        val completion = async(start = CoroutineStart.UNDISPATCHED) {
            engine.focusCompletions.first()
        }

        engine.start(debugSettings)
        elapsedNanos = 1_000_000_000L
        wallTime += 1_000L
        engine.tick()

        assertEquals(
            FocusCompletion(
                startTime = 1_000_000L,
                endTime = 1_001_000L,
                durationMs = 1_000L,
                completedRound = 1,
                taskId = null
            ),
            completion.await()
        )
        assertEquals(1, engine.state.value.completedFocusRounds)
        assertEquals(TimerPhase.SHORT_BREAK, engine.state.value.phase)
        engine.tick()
        assertEquals(1, engine.state.value.completedFocusRounds)
        engine.stop(debugSettings)
    }

    @Test
    fun restore_advancesExpiredFocusAndPreservesRemainingBreakTime() = runBlocking {
        val elapsedNanos = 0L
        val now = 5_000_000L
        val engine = TimerEngine(
            nanoTime = { elapsedNanos },
            currentTimeMillis = { now }
        )
        val debugSettings = settings.copy(
            debugModeEnabled = true,
            debugFocusDurationSec = 1
        )
        val completion = async(start = CoroutineStart.UNDISPATCHED) {
            engine.focusCompletions.first()
        }

        engine.restore(
            TimerSnapshot(
                phase = TimerPhase.FOCUS,
                activePhase = TimerPhase.FOCUS,
                remainingMs = 1_000L,
                totalMs = 1_000L,
                completedFocusRounds = 0,
                isRunning = true,
                isFaceDown = false,
                deadlineEpochMs = now - 1_000L
            ),
            debugSettings
        )

        assertEquals(now - 2_000L, completion.await().startTime)
        assertEquals(now - 1_000L, completion.await().endTime)
        assertEquals(TimerPhase.SHORT_BREAK, engine.state.value.phase)
        assertEquals(299_000L, engine.state.value.remainingMs)
        assertEquals(1, engine.state.value.completedFocusRounds)
        engine.stop(debugSettings)
    }

    @Test
    fun updatedSettings_areUsedForNextPhase() {
        var elapsedNanos = 0L
        val engine = TimerEngine(
            nanoTime = { elapsedNanos },
            currentTimeMillis = { 0L }
        )
        val initial = settings.copy(
            debugModeEnabled = true,
            debugFocusDurationSec = 1,
            shortBreakMin = 5
        )

        engine.start(initial)
        engine.updateSettings(initial.copy(shortBreakMin = 10))
        elapsedNanos = 1_000_000_000L
        engine.tick()

        assertEquals(TimerPhase.SHORT_BREAK, engine.state.value.phase)
        assertEquals(10 * 60_000L, engine.state.value.totalMs)
        engine.stop(initial)
    }

    @Test
    fun taskSelection_isFrozenForCurrentFocusAndAppliedToNextFocus() = runBlocking {
        var elapsedNanos = 0L
        val engine = TimerEngine(
            nanoTime = { elapsedNanos },
            currentTimeMillis = { 1_000L }
        )
        val debugSettings = settings.copy(
            debugModeEnabled = true,
            debugFocusDurationSec = 1
        )
        engine.selectTask(42L)
        val completion = async(start = CoroutineStart.UNDISPATCHED) {
            engine.focusCompletions.first()
        }

        engine.start(debugSettings)
        engine.selectTask(99L)
        elapsedNanos = 1_000_000_000L
        engine.tick()

        assertEquals(42L, completion.await().taskId)
        engine.skip(debugSettings)
        assertEquals(TimerPhase.FOCUS, engine.state.value.phase)
        assertEquals(99L, engine.state.value.taskId)
        engine.stop(debugSettings)
    }
}
