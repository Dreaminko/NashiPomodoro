package com.dreaminko.nashipomodoro.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["isCompleted", "createdAt"])]
)
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val pomodoroGoal: Int = 1,
    val pomodoroDone: Int = 0,
    val createdAt: Long
)
