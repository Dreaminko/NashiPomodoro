package com.example.nashitimer.core.timer

import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.TimerPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class FocusCompletion(
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val completedRound: Int,
    val taskId: Long?
)

data class TimerSnapshot(
    val phase: TimerPhase,
    val activePhase: TimerPhase,
    val remainingMs: Long,
    val totalMs: Long,
    val completedFocusRounds: Int,
    val isRunning: Boolean,
    val isFaceDown: Boolean,
    val deadlineEpochMs: Long,
    val taskId: Long? = null
)

@Singleton
class TimerEngine internal constructor(
    private val nanoTime: () -> Long,
    private val currentTimeMillis: () -> Long
) {
    @Inject
    constructor() : this(System::nanoTime, System::currentTimeMillis)

    private val lock = Any()
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()
    private val focusCompletionChannel = Channel<FocusCompletion>(Channel.UNLIMITED)
    val focusCompletions: Flow<FocusCompletion> = focusCompletionChannel.receiveAsFlow()

    private val tickerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null
    private var tickerGeneration = 0L
    private var pausedPhase = TimerPhase.FOCUS
    private var deadlineNanos = 0L
    private var currentSettings = AppSettings()
    private var selectedTaskId: Long? = null

    fun start(settings: AppSettings, phase: TimerPhase = TimerPhase.FOCUS) {
        synchronized(lock) {
            currentSettings = settings.normalized()
            val duration = durationFor(phase, currentSettings)
            _state.value = _state.value.copy(
                phase = phase,
                remainingMs = duration,
                totalMs = duration,
                isRunning = true,
                taskId = if (phase == TimerPhase.FOCUS) selectedTaskId else null
            )
            runTickerLocked()
        }
    }

    fun resume(settings: AppSettings) {
        synchronized(lock) {
            currentSettings = settings.normalized()
            if (_state.value.phase == TimerPhase.IDLE) {
                val duration = durationFor(TimerPhase.FOCUS, currentSettings)
                _state.value = _state.value.copy(
                    phase = TimerPhase.FOCUS,
                    remainingMs = duration,
                    totalMs = duration,
                    isRunning = true,
                    taskId = selectedTaskId
                )
            } else {
                _state.value = _state.value.copy(
                    isRunning = true,
                    phase = activePhaseLocked()
                )
            }
            runTickerLocked()
        }
    }

    fun pause() {
        synchronized(lock) {
            if (!_state.value.isRunning) return
            val remainingMs = remainingUntilDeadlineLocked()
            cancelTickerLocked()
            pausedPhase = _state.value.phase
            _state.value = _state.value.copy(
                remainingMs = remainingMs,
                isRunning = false,
                phase = TimerPhase.PAUSED
            )
        }
    }

    fun stop(settings: AppSettings) {
        stop(settings.focusDurationMs)
    }

    fun stop(focusDurationMs: Long) {
        synchronized(lock) {
            cancelTickerLocked()
            pausedPhase = TimerPhase.FOCUS
            val duration = focusDurationMs.coerceAtLeast(0L)
            _state.value = TimerState(remainingMs = duration, totalMs = duration)
        }
    }

    fun skip(settings: AppSettings) {
        synchronized(lock) {
            currentSettings = settings.normalized()
            val current = _state.value
            if (current.phase == TimerPhase.IDLE) return

            cancelTickerLocked()
            val currentPhase = if (current.phase == TimerPhase.PAUSED) {
                pausedPhase
            } else {
                current.phase
            }
            val nextPhase = when (currentPhase) {
                TimerPhase.FOCUS -> TimerPhase.SHORT_BREAK
                TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> TimerPhase.FOCUS
                TimerPhase.IDLE, TimerPhase.PAUSED -> TimerPhase.FOCUS
            }
            val duration = durationFor(nextPhase, currentSettings)
            _state.value = current.copy(
                phase = nextPhase,
                remainingMs = duration,
                totalMs = duration,
                isRunning = current.isRunning,
                taskId = if (nextPhase == TimerPhase.FOCUS) selectedTaskId else null
            )
            if (current.isRunning) runTickerLocked()
        }
    }

    fun setFaceDown(faceDown: Boolean) {
        synchronized(lock) {
            _state.value = _state.value.copy(isFaceDown = faceDown)
        }
    }

    fun selectTask(taskId: Long?) {
        synchronized(lock) {
            selectedTaskId = taskId
            val current = _state.value
            val canUpdateCurrentPhase = current.phase == TimerPhase.IDLE ||
                current.phase == TimerPhase.SHORT_BREAK ||
                current.phase == TimerPhase.LONG_BREAK
            if (canUpdateCurrentPhase) {
                _state.value = current.copy(taskId = taskId)
            }
        }
    }

    fun updateSettings(settings: AppSettings) {
        synchronized(lock) {
            currentSettings = settings.normalized()
            if (_state.value.phase == TimerPhase.IDLE) {
                val duration = settings.focusDurationMs
                _state.value = _state.value.copy(
                    remainingMs = duration,
                    totalMs = duration
                )
            }
        }
    }

    fun snapshot(): TimerSnapshot = synchronized(lock) {
        val current = _state.value
        val remainingMs = if (current.isRunning) {
            remainingUntilDeadlineLocked()
        } else {
            current.remainingMs
        }
        TimerSnapshot(
            phase = current.phase,
            activePhase = if (current.phase == TimerPhase.PAUSED) {
                pausedPhase
            } else {
                current.phase
            },
            remainingMs = remainingMs,
            totalMs = current.totalMs,
            completedFocusRounds = current.completedFocusRounds,
            isRunning = current.isRunning,
            isFaceDown = current.isFaceDown,
            deadlineEpochMs = if (current.isRunning) {
                currentTimeMillis() + remainingMs
            } else {
                0L
            },
            taskId = current.taskId
        )
    }

    fun restore(snapshot: TimerSnapshot, settings: AppSettings) {
        synchronized(lock) {
            cancelTickerLocked()
            currentSettings = settings.normalized()
            pausedPhase = snapshot.activePhase
            _state.value = TimerState(
                phase = snapshot.phase,
                remainingMs = snapshot.remainingMs.coerceIn(0L, snapshot.totalMs),
                totalMs = snapshot.totalMs,
                completedFocusRounds = snapshot.completedFocusRounds.coerceAtLeast(0),
                isRunning = snapshot.isRunning,
                isFaceDown = snapshot.isFaceDown,
                taskId = snapshot.taskId
            )

            if (!snapshot.isRunning) return

            val now = currentTimeMillis()
            var deadline = snapshot.deadlineEpochMs
            var restoredTransitions = 0
            while (deadline <= now && restoredTransitions < MAX_RESTORED_TRANSITIONS) {
                _state.value = _state.value.copy(remainingMs = 0L)
                advanceLocked(completionEndTime = deadline)
                deadline += _state.value.totalMs
                restoredTransitions += 1
            }
            if (deadline <= now) {
                val duration = currentSettings.focusDurationMs
                _state.value = TimerState(remainingMs = duration, totalMs = duration)
                return
            }
            val remainingMs = (deadline - now).coerceAtLeast(1L)
            _state.value = _state.value.copy(remainingMs = remainingMs)
            runTickerLocked()
        }
    }

    internal fun tick() {
        synchronized(lock) {
            if (_state.value.isRunning) updateRemainingLocked()
        }
    }

    private fun runTickerLocked() {
        cancelTickerLocked()
        deadlineNanos = nanoTime() + _state.value.remainingMs * NANOS_PER_MILLISECOND
        val generation = tickerGeneration
        ticker = tickerScope.launch {
            while (true) {
                val delayMs = synchronized(lock) {
                    if (generation != tickerGeneration || !_state.value.isRunning) {
                        null
                    } else {
                        updateRemainingLocked()
                        minOf(
                            TICK_INTERVAL_MS,
                            _state.value.remainingMs.coerceAtLeast(1L)
                        )
                    }
                }
                if (delayMs == null) return@launch
                delay(delayMs)
            }
        }
    }

    private fun updateRemainingLocked() {
        val next = remainingUntilDeadlineLocked()
        _state.value = _state.value.copy(remainingMs = next)
        if (next == 0L) {
            advanceLocked()
            deadlineNanos = nanoTime() + _state.value.remainingMs * NANOS_PER_MILLISECOND
        }
    }

    private fun cancelTickerLocked() {
        tickerGeneration += 1
        ticker?.cancel()
        ticker = null
    }

    private fun remainingUntilDeadlineLocked(): Long {
        val remainingNanos = (deadlineNanos - nanoTime()).coerceAtLeast(0L)
        return (remainingNanos + NANOS_PER_MILLISECOND - 1) / NANOS_PER_MILLISECOND
    }

    private fun advanceLocked(completionEndTime: Long = currentTimeMillis()) {
        val current = _state.value
        val finishedFocus = current.phase == TimerPhase.FOCUS
        val rounds = if (finishedFocus) current.completedFocusRounds + 1 else current.completedFocusRounds
        val nextPhase = when {
            finishedFocus && rounds % currentSettings.safeLongBreakInterval == 0 ->
                TimerPhase.LONG_BREAK
            finishedFocus -> TimerPhase.SHORT_BREAK
            else -> TimerPhase.FOCUS
        }
        val duration = durationFor(nextPhase, currentSettings)
        _state.value = current.copy(
            phase = nextPhase,
            remainingMs = duration,
            totalMs = duration,
            completedFocusRounds = rounds,
            isRunning = true,
            taskId = if (nextPhase == TimerPhase.FOCUS) selectedTaskId else null
        )
        if (finishedFocus) {
            focusCompletionChannel.trySend(
                FocusCompletion(
                    startTime = completionEndTime - current.totalMs,
                    endTime = completionEndTime,
                    durationMs = current.totalMs,
                    completedRound = rounds,
                    taskId = current.taskId
                )
            )
        }
    }

    private fun activePhaseLocked(): TimerPhase = when (_state.value.phase) {
        TimerPhase.PAUSED -> pausedPhase
        TimerPhase.IDLE -> TimerPhase.FOCUS
        else -> _state.value.phase
    }

    private fun durationFor(phase: TimerPhase, settings: AppSettings): Long {
        return when (phase) {
            TimerPhase.FOCUS, TimerPhase.IDLE, TimerPhase.PAUSED -> settings.focusDurationMs
            TimerPhase.SHORT_BREAK -> settings.shortBreakDurationMs
            TimerPhase.LONG_BREAK -> settings.longBreakDurationMs
        }
    }

    private companion object {
        const val TICK_INTERVAL_MS = 1_000L
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val MAX_RESTORED_TRANSITIONS = 10_000
    }
}
