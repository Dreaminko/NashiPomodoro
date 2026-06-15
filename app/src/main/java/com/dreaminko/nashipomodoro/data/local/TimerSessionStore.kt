package com.dreaminko.nashipomodoro.data.local

import android.content.Context
import androidx.core.content.edit
import com.dreaminko.nashipomodoro.core.timer.TimerSnapshot
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerSessionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun load(): TimerSnapshot? {
        if (!preferences.getBoolean(KEY_PRESENT, false)) return null
        val phase = preferences.enumValue<TimerPhase>(KEY_PHASE) ?: return null
        val activePhase = preferences.enumValue<TimerPhase>(KEY_ACTIVE_PHASE) ?: return null
        val totalMs = preferences.getLong(KEY_TOTAL_MS, 0L)
        if (totalMs !in 1L..MAX_PHASE_DURATION_MS) return null
        val isRunning = preferences.getBoolean(KEY_RUNNING, false)
        val deadlineEpochMs = preferences.getLong(KEY_DEADLINE_EPOCH_MS, 0L)
        if (isRunning && (phase == TimerPhase.IDLE || phase == TimerPhase.PAUSED)) return null
        if (isRunning && deadlineEpochMs <= 0L) return null
        if (isRunning && deadlineEpochMs > System.currentTimeMillis() + totalMs + CLOCK_SKEW_MS) {
            return null
        }
        if (!isRunning && phase == TimerPhase.PAUSED &&
            activePhase !in ACTIVE_PHASES
        ) {
            return null
        }

        return TimerSnapshot(
            phase = phase,
            activePhase = activePhase,
            remainingMs = preferences.getLong(KEY_REMAINING_MS, totalMs)
                .coerceIn(0L, totalMs),
            totalMs = totalMs,
            completedFocusRounds = preferences.getInt(KEY_COMPLETED_ROUNDS, 0)
                .coerceAtLeast(0),
            isRunning = isRunning,
            isFaceDown = false,
            deadlineEpochMs = deadlineEpochMs,
            taskId = preferences.getLong(KEY_TASK_ID, NO_TASK_ID)
                .takeIf { it > 0L }
        )
    }

    fun save(snapshot: TimerSnapshot) {
        preferences.edit {
            putBoolean(KEY_PRESENT, true)
            putString(KEY_PHASE, snapshot.phase.name)
            putString(KEY_ACTIVE_PHASE, snapshot.activePhase.name)
            putLong(KEY_REMAINING_MS, snapshot.remainingMs)
            putLong(KEY_TOTAL_MS, snapshot.totalMs)
            putInt(KEY_COMPLETED_ROUNDS, snapshot.completedFocusRounds)
            putBoolean(KEY_RUNNING, snapshot.isRunning)
            putLong(KEY_DEADLINE_EPOCH_MS, snapshot.deadlineEpochMs)
            putLong(KEY_TASK_ID, snapshot.taskId ?: NO_TASK_ID)
        }
    }

    fun clear() {
        preferences.edit { clear() }
    }

    private inline fun <reified T : Enum<T>> android.content.SharedPreferences.enumValue(
        key: String
    ): T? = getString(key, null)?.let { stored ->
        enumValues<T>().firstOrNull { it.name == stored }
    }

    private companion object {
        const val FILE_NAME = "active_timer_session"
        const val KEY_PRESENT = "present"
        const val KEY_PHASE = "phase"
        const val KEY_ACTIVE_PHASE = "active_phase"
        const val KEY_REMAINING_MS = "remaining_ms"
        const val KEY_TOTAL_MS = "total_ms"
        const val KEY_COMPLETED_ROUNDS = "completed_rounds"
        const val KEY_RUNNING = "running"
        const val KEY_DEADLINE_EPOCH_MS = "deadline_epoch_ms"
        const val KEY_TASK_ID = "task_id"
        const val NO_TASK_ID = -1L
        const val MAX_PHASE_DURATION_MS = 24 * 60 * 60_000L
        const val CLOCK_SKEW_MS = 60_000L
        val ACTIVE_PHASES = setOf(
            TimerPhase.FOCUS,
            TimerPhase.SHORT_BREAK,
            TimerPhase.LONG_BREAK
        )
    }
}
