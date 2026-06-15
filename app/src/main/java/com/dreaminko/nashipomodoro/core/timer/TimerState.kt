package com.dreaminko.nashipomodoro.core.timer

import com.dreaminko.nashipomodoro.domain.model.TimerPhase

data class TimerState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val remainingMs: Long = 25 * 60 * 1000L,
    val totalMs: Long = 25 * 60 * 1000L,
    val completedFocusRounds: Int = 0,
    val isRunning: Boolean = false,
    val isFaceDown: Boolean = false,
    val taskId: Long? = null
) {
    val progress: Float
        get() = if (totalMs <= 0) 0f else 1f - (remainingMs.toFloat() / totalMs.toFloat())

    val remainingFraction: Float
        get() = if (totalMs <= 0) 0f else (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    val timeText: String
        get() {
            val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
            return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
}
