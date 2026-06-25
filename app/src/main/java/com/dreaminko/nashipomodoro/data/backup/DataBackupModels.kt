package com.dreaminko.nashipomodoro.data.backup

import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem

data class DataBackupPayload(
    val schemaVersion: Int,
    val exportedAt: Long,
    val appVersion: String,
    val settings: AppSettings,
    val tasks: List<TaskItem>,
    val sessions: List<PomodoroSession>
)

data class DataImportSummary(
    val tasksAdded: Int,
    val tasksUpdated: Int,
    val sessionsAdded: Int,
    val sessionsSkipped: Int
)

class DataBackupException(message: String, cause: Throwable? = null) : Exception(message, cause)
