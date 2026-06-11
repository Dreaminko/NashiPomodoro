package com.example.nashitimer.core.timer

import com.example.nashitimer.domain.model.TimerPhase

data class TimerState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val remainingMs: Long = 25 * 60 * 1000L,
    val totalMs: Long = 25 * 60 * 1000L,
    val completedFocusRounds: Int = 0,
    val isRunning: Boolean = false,
    val isFaceDown: Boolean = false
) {
    val progress: Float
        get() = if (totalMs <= 0) 0f else 1f - (remainingMs.toFloat() / totalMs.toFloat())

    val timeText: String
        get() {
            val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
            return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
}
