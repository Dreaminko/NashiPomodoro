package com.dreaminko.nashipomodoro.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pomodoro_sessions",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["taskId"]),
        Index(value = ["startTime", "endTime", "phase"], unique = true)
    ]
)
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
