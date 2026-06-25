package com.dreaminko.nashipomodoro.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dreaminko.nashipomodoro.data.local.PomodoroDatabase
import com.dreaminko.nashipomodoro.data.local.SettingsStore
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class DataBackupRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = Room.inMemoryDatabaseBuilder(context, PomodoroDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val dao = database.pomodoroDao()
    private val repository = DataBackupRepository(context, database, SettingsStore(context))

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun importPayload_mergesTasksAndRemapsSessionTaskIds() = runBlocking {
        dao.upsertTask(
            TaskItem(
                id = 1,
                title = "Ship backup",
                description = "Manual file",
                pomodoroGoal = 2,
                pomodoroDone = 1,
                createdAt = 100L
            )
        )
        dao.insertSession(
            PomodoroSession(
                startTime = 1_000L,
                endTime = 2_000L,
                phase = "FOCUS",
                durationMs = 1_000L,
                completed = true,
                taskId = 1L,
                createdAt = 2_000L
            )
        )

        val summary = repository.importPayload(
            DataBackupPayload(
                schemaVersion = DataBackupJson.SCHEMA_VERSION,
                exportedAt = 3_000L,
                appVersion = "1.0",
                settings = AppSettings(debugModeEnabled = true, debugFocusDurationSec = 1),
                tasks = listOf(
                    TaskItem(
                        id = 77,
                        title = "Ship backup",
                        description = "Manual file",
                        pomodoroGoal = 2,
                        pomodoroDone = 2,
                        createdAt = 100L
                    ),
                    TaskItem(
                        id = 88,
                        title = "Plan next release",
                        pomodoroGoal = 1,
                        createdAt = 300L
                    )
                ),
                sessions = listOf(
                    PomodoroSession(
                        id = 91,
                        startTime = 1_000L,
                        endTime = 2_000L,
                        phase = "FOCUS",
                        durationMs = 1_000L,
                        completed = true,
                        taskId = 77L,
                        createdAt = 2_000L
                    ),
                    PomodoroSession(
                        id = 92,
                        startTime = 3_000L,
                        endTime = 4_000L,
                        phase = "FOCUS",
                        durationMs = 1_000L,
                        completed = true,
                        taskId = 88L,
                        createdAt = 4_000L
                    )
                )
            )
        )

        val tasks = dao.tasksSnapshot()
        val sessions = dao.allSessionsSnapshot()
        val mergedTask = tasks.single { it.title == "Ship backup" }
        val addedTask = tasks.single { it.title == "Plan next release" }
        val addedSession = sessions.single { it.startTime == 3_000L }

        assertEquals(1, summary.tasksAdded)
        assertEquals(1, summary.tasksUpdated)
        assertEquals(1, summary.sessionsAdded)
        assertEquals(1, summary.sessionsSkipped)
        assertEquals(2, mergedTask.pomodoroDone)
        assertTrue(mergedTask.isCompleted)
        assertEquals(addedTask.id, addedSession.taskId)
    }

    @Test
    fun importPayload_recalculatesExistingTaskProgressFromMergedSessions() = runBlocking {
        dao.upsertTask(
            TaskItem(
                id = 1,
                title = "Cross-device task",
                pomodoroGoal = 3,
                pomodoroDone = 1,
                createdAt = 500L
            )
        )
        dao.insertSession(
            PomodoroSession(
                startTime = 10_000L,
                endTime = 20_000L,
                phase = "FOCUS",
                durationMs = 10_000L,
                completed = true,
                taskId = 1L,
                createdAt = 20_000L
            )
        )

        val summary = repository.importPayload(
            DataBackupPayload(
                schemaVersion = DataBackupJson.SCHEMA_VERSION,
                exportedAt = 30_000L,
                appVersion = "1.0",
                settings = AppSettings(),
                tasks = listOf(
                    TaskItem(
                        id = 77L,
                        title = "Cross-device task",
                        pomodoroGoal = 3,
                        pomodoroDone = 1,
                        createdAt = 500L
                    )
                ),
                sessions = listOf(
                    PomodoroSession(
                        id = 91L,
                        startTime = 30_000L,
                        endTime = 40_000L,
                        phase = "FOCUS",
                        durationMs = 10_000L,
                        completed = true,
                        taskId = 77L,
                        createdAt = 40_000L
                    )
                )
            )
        )

        val task = dao.tasksSnapshot().single()

        assertEquals(0, summary.tasksAdded)
        assertEquals(1, summary.tasksUpdated)
        assertEquals(1, summary.sessionsAdded)
        assertEquals(0, summary.sessionsSkipped)
        assertEquals(2, task.pomodoroDone)
    }

    @Test
    fun backupReader_rejectsOversizedStreamsWithoutKnownLength() {
        val oversizedBytes = ByteArray((DataBackupReader.MAX_BACKUP_BYTES + 1L).toInt())

        assertThrows(DataBackupException::class.java) {
            DataBackupReader.read(ByteArrayInputStream(oversizedBytes))
        }
    }
}