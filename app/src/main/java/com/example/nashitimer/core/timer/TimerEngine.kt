package com.example.nashitimer.core.timer

import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerEngine @Inject constructor() {
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()
    private val tickerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null
    private var pausedPhase = TimerPhase.FOCUS
    private var deadlineNanos = 0L

    fun start(settings: AppSettings, phase: TimerPhase = TimerPhase.FOCUS) {
        val duration = durationFor(phase, settings)
        _state.value = _state.value.copy(
            phase = phase,
            remainingMs = duration,
            totalMs = duration,
            isRunning = true
        )
        runTicker(settings)
    }

    fun resume(settings: AppSettings) {
        if (_state.value.phase == TimerPhase.IDLE) {
            start(settings)
        } else {
            _state.value = _state.value.copy(isRunning = true, phase = activePhase())
            runTicker(settings)
        }
    }

    fun pause() {
        if (!_state.value.isRunning) return
        val remainingMs = remainingUntilDeadline()
        ticker?.cancel()
        pausedPhase = _state.value.phase
        _state.value = _state.value.copy(
            remainingMs = remainingMs,
            isRunning = false,
            phase = TimerPhase.PAUSED
        )
    }

    fun stop(settings: AppSettings) {
        stop(settings.focusDurationMs)
    }

    fun stop(focusDurationMs: Long) {
        ticker?.cancel()
        pausedPhase = TimerPhase.FOCUS
        val duration = focusDurationMs.coerceAtLeast(0L)
        _state.value = TimerState(remainingMs = duration, totalMs = duration)
    }

    fun skip(settings: AppSettings) {
        val current = _state.value
        if (current.phase == TimerPhase.IDLE) return

        ticker?.cancel()
        val currentPhase = if (current.phase == TimerPhase.PAUSED) pausedPhase else current.phase
        val nextPhase = when (currentPhase) {
            TimerPhase.FOCUS -> {
                val nextRound = current.completedFocusRounds + 1
                if (nextRound % settings.longBreakInterval == 0) {
                    TimerPhase.LONG_BREAK
                } else {
                    TimerPhase.SHORT_BREAK
                }
            }
            TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> TimerPhase.FOCUS
            TimerPhase.IDLE, TimerPhase.PAUSED -> TimerPhase.FOCUS
        }
        val duration = durationFor(nextPhase, settings)
        _state.value = current.copy(
            phase = nextPhase,
            remainingMs = duration,
            totalMs = duration,
            isRunning = current.isRunning
        )
        if (current.isRunning) runTicker(settings)
    }

    fun setFaceDown(faceDown: Boolean) {
        _state.value = _state.value.copy(isFaceDown = faceDown)
    }

    private fun runTicker(settings: AppSettings) {
        ticker?.cancel()
        deadlineNanos = System.nanoTime() + _state.value.remainingMs * NANOS_PER_MILLISECOND
        ticker = tickerScope.launch {
            while (_state.value.isRunning) {
                val next = remainingUntilDeadline()
                _state.value = _state.value.copy(remainingMs = next)
                if (next == 0L) {
                    advance(settings)
                    deadlineNanos =
                        System.nanoTime() + _state.value.remainingMs * NANOS_PER_MILLISECOND
                }
                delay(minOf(TICK_INTERVAL_MS, _state.value.remainingMs.coerceAtLeast(1L)))
            }
        }
    }

    private fun remainingUntilDeadline(): Long {
        val remainingNanos = (deadlineNanos - System.nanoTime()).coerceAtLeast(0L)
        return (remainingNanos + NANOS_PER_MILLISECOND - 1) / NANOS_PER_MILLISECOND
    }

    private fun advance(settings: AppSettings) {
        val current = _state.value
        val finishedFocus = current.phase == TimerPhase.FOCUS
        val rounds = if (finishedFocus) current.completedFocusRounds + 1 else current.completedFocusRounds
        val nextPhase = when {
            finishedFocus && rounds % settings.longBreakInterval == 0 -> TimerPhase.LONG_BREAK
            finishedFocus -> TimerPhase.SHORT_BREAK
            else -> TimerPhase.FOCUS
        }
        val duration = durationFor(nextPhase, settings)
        _state.value = current.copy(
            phase = nextPhase,
            remainingMs = duration,
            totalMs = duration,
            completedFocusRounds = rounds,
            isRunning = true
        )
    }

    private fun activePhase(): TimerPhase = when (_state.value.phase) {
        TimerPhase.PAUSED -> pausedPhase
        TimerPhase.IDLE -> TimerPhase.FOCUS
        else -> _state.value.phase
    }

    private fun durationFor(phase: TimerPhase, settings: AppSettings): Long {
        return when (phase) {
            TimerPhase.FOCUS, TimerPhase.IDLE, TimerPhase.PAUSED -> settings.focusDurationMs
            TimerPhase.SHORT_BREAK -> settings.shortBreakMin * 60_000L
            TimerPhase.LONG_BREAK -> settings.longBreakMin * 60_000L
        }
    }

    private companion object {
        const val TICK_INTERVAL_MS = 1_000L
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}
