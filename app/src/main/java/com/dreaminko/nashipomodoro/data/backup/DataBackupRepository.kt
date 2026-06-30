package com.dreaminko.nashipomodoro.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.dreaminko.nashipomodoro.BuildConfig
import com.dreaminko.nashipomodoro.data.local.PomodoroDatabase
import com.dreaminko.nashipomodoro.data.local.SettingsStore
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DataBackupRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: PomodoroDatabase,
    private val settingsStore: SettingsStore
) {
    private val dao = database.pomodoroDao()

    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val payload = DataBackupPayload(
            schemaVersion = DataBackupJson.SCHEMA_VERSION,
            exportedAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            settings = settingsStore.currentSettings(),
            tasks = dao.tasksSnapshot(),
            sessions = dao.allSessionsSnapshot()
        )
        val raw = DataBackupJson.encode(payload)
        val resolver = context.contentResolver
        val stream = resolver.openOutputStream(uri, "wt")
            ?: throw DataBackupException("Unable to open the selected backup file.")
        try {
            stream.bufferedWriter(Charsets.UTF_8).use { it.write(raw) }
        } catch (error: IOException) {
            throw DataBackupException("Unable to write backup file.", error)
        }
    }

    suspend fun importFrom(uri: Uri): DataImportSummary = withContext(Dispatchers.IO) {
        importPayload(DataBackupJson.decode(readBackupText(uri)))
    }

    suspend fun importPayload(payload: DataBackupPayload): DataImportSummary {
        val previousSettings = settingsStore.currentSettings()
        runCatching { settingsStore.importUserSettings(payload.settings) }
            .getOrElse { error ->
                throw DataBackupException("Unable to restore settings from backup.", error)
            }
        return try {
            mergePayloadIntoDatabase(payload)
        } catch (error: Exception) {
            val rollbackError = runCatching {
                settingsStore.importUserSettings(previousSettings)
            }.exceptionOrNull()
            if (rollbackError != null) {
                rollbackError.addSuppressed(error)
                throw DataBackupException(
                    "Backup import failed after applying settings, and the previous settings could not be restored.",
                    rollbackError
                )
            }
            throw DataBackupException(
                "Backup import failed. Settings were restored to their previous values.",
                error
            )
        }
    }

    private suspend fun mergePayloadIntoDatabase(
        payload: DataBackupPayload
    ): DataImportSummary = database.withTransaction {
            val taskIdMap = mutableMapOf<Long, Long>()
            var tasksAdded = 0
            var tasksUpdated = 0
            val addedTaskIds = mutableSetOf<Long>()
            val updatedTaskIds = mutableSetOf<Long>()
            val existingByFingerprint = dao.tasksSnapshot()
                .associateBy { it.backupFingerprint() }
                .toMutableMap()

            payload.tasks.forEach { imported ->
                val fingerprint = imported.backupFingerprint()
                val existing = existingByFingerprint[fingerprint]
                if (existing == null) {
                    val localTask = imported.copy(id = 0)
                    val localId = dao.upsertTask(localTask)
                    taskIdMap[imported.id] = localId
                    addedTaskIds += localId
                    existingByFingerprint[fingerprint] = localTask.copy(id = localId)
                    tasksAdded += 1
                } else {
                    taskIdMap[imported.id] = existing.id
                    val merged = existing.mergeImported(imported)
                    if (merged != existing) {
                        dao.updateTask(merged)
                        updatedTaskIds += existing.id
                        existingByFingerprint[fingerprint] = merged
                        tasksUpdated += 1
                    }
                }
            }

            val sessions = payload.sessions.map { imported ->
                imported.copy(
                    id = 0,
                    taskId = imported.taskId?.let { taskIdMap[it] }
                )
            }
            val insertedIds = if (sessions.isEmpty()) {
                emptyList()
            } else {
                dao.insertSessions(sessions)
            }
            val sessionsAdded = insertedIds.count { it != -1L }
            taskIdMap.values.toSet().forEach { taskId ->
                val task = dao.taskSnapshot(taskId) ?: return@forEach
                val sessionDone = dao.completedSessionCountForTask(
                    taskId,
                    TimerPhase.FOCUS.name
                )
                val recalculatedDone = max(task.pomodoroDone, sessionDone)
                val recalculated = task.copy(
                    pomodoroDone = recalculatedDone,
                    isCompleted = task.isCompleted || recalculatedDone >= task.pomodoroGoal
                )
                if (recalculated != task) {
                    dao.updateTask(recalculated)
                    if (taskId !in addedTaskIds && updatedTaskIds.add(taskId)) {
                        tasksUpdated += 1
                    }
                }
            }

            DataImportSummary(
                tasksAdded = tasksAdded,
                tasksUpdated = tasksUpdated,
                sessionsAdded = sessionsAdded,
                sessionsSkipped = insertedIds.size - sessionsAdded
            )
        }

    private fun readBackupText(uri: Uri): String {
        val resolver = context.contentResolver
        try {
            resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                DataBackupReader.rejectIfTooLarge(descriptor.length)
            }
            val stream = resolver.openInputStream(uri)
                ?: throw DataBackupException("Unable to open the selected backup file.")
            return DataBackupReader.read(stream)
        } catch (error: DataBackupException) {
            throw error
        } catch (error: IOException) {
            throw DataBackupException("Unable to read backup file.", error)
        }
    }

}

internal object DataBackupReader {
    const val MAX_BACKUP_BYTES = 5L * 1024L * 1024L

    fun rejectIfTooLarge(length: Long) {
        if (length > MAX_BACKUP_BYTES) {
            throw DataBackupException("Backup file is too large.")
        }
    }

    fun read(input: InputStream): String = input.use {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var totalBytes = 0L
        while (true) {
            val bytesRead = it.read(buffer)
            if (bytesRead == -1) break
            totalBytes += bytesRead
            if (totalBytes > MAX_BACKUP_BYTES) {
                throw DataBackupException("Backup file is too large.")
            }
            output.write(buffer, 0, bytesRead)
        }
        String(output.toByteArray(), Charsets.UTF_8)
    }
}

private fun TaskItem.backupFingerprint(): String = listOf(
    title,
    description.orEmpty(),
    createdAt.toString(),
    pomodoroGoal.toString()
).joinToString(separator = "\u001F")

private fun TaskItem.mergeImported(imported: TaskItem): TaskItem {
    val mergedDone = max(pomodoroDone, imported.pomodoroDone)
    val mergedCompleted = isCompleted || imported.isCompleted || mergedDone >= pomodoroGoal
    return copy(
        pomodoroDone = mergedDone,
        isCompleted = mergedCompleted
    )
}
