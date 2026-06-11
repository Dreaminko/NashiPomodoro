package com.example.nashitimer.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val phase: String,
    val durationMs: Long,
    val completed: Boolean,
    val taskId: Long? = null,
    val tag: String? = null,
    val createdAt: Long
)
