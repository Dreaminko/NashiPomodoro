package com.example.nashitimer.core.timer

import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    private var ticker: Job? = null

    fun start(scope: CoroutineScope, settings: AppSettings, phase: TimerPhase = TimerPhase.FOCUS) {
        val duration = durationFor(phase, settings)
        _state.value = _state.value.copy(
            phase = phase,
            remainingMs = duration,
            totalMs = duration,
            isRunning = true
        )
        runTicker(scope, settings)
    }

    fun resume(scope: CoroutineScope, settings: AppSettings) {
        if (_state.value.phase == TimerPhase.IDLE) {
            start(scope, settings)
        } else {
            _state.value = _state.value.copy(isRunning = true, phase = activePhase())
            runTicker(scope, settings)
        }
    }

    fun pause() {
        ticker?.cancel()
        _state.value = _state.value.copy(isRunning = false, phase = TimerPhase.PAUSED)
    }

    fun stop(settings: AppSettings) {
        ticker?.cancel()
        val duration = durationFor(TimerPhase.FOCUS, settings)
        _state.value = TimerState(remainingMs = duration, totalMs = duration)
    }

    fun setFaceDown(faceDown: Boolean) {
        _state.value = _state.value.copy(isFaceDown = faceDown)
    }

    private fun runTicker(scope: CoroutineScope, settings: AppSettings) {
        ticker?.cancel()
        ticker = scope.launch {
            while (_state.value.isRunning) {
                delay(1000)
                val next = (_state.value.remainingMs - 1000).coerceAtLeast(0)
                _state.value = _state.value.copy(remainingMs = next)
                if (next == 0L) advance(settings)
            }
        }
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
        TimerPhase.PAUSED -> TimerPhase.FOCUS
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
}
