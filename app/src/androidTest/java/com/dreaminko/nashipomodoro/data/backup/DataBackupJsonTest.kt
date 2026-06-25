package com.dreaminko.nashipomodoro.data.backup

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import com.dreaminko.nashipomodoro.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataBackupJsonTest {
    @Test
    fun roundTrip_excludesDebugSettings() {
        val payload = DataBackupPayload(
            schemaVersion = DataBackupJson.SCHEMA_VERSION,
            exportedAt = 123L,
            appVersion = "1.0",
            settings = AppSettings(
                themeMode = ThemeMode.LIGHT,
                debugModeEnabled = true,
                debugFocusDurationSec = 5
            ),
            tasks = listOf(
                TaskItem(
                    id = 7,
                    title = "Write release notes",
                    description = "Short version",
                    pomodoroGoal = 2,
                    pomodoroDone = 1,
                    createdAt = 10L
                )
            ),
            sessions = listOf(
                PomodoroSession(
                    id = 9,
                    startTime = 100L,
                    endTime = 200L,
                    phase = "FOCUS",
                    durationMs = 100L,
                    completed = true,
                    taskId = 7L,
                    tag = "Focus",
                    createdAt = 200L
                )
            )
        )

        val decoded = DataBackupJson.decode(DataBackupJson.encode(payload))

        assertEquals(ThemeMode.LIGHT, decoded.settings.themeMode)
        assertFalse(decoded.settings.debugModeEnabled)
        assertEquals(30, decoded.settings.debugFocusDurationSec)
        assertEquals(payload.tasks, decoded.tasks)
        assertEquals(payload.sessions, decoded.sessions)
    }

    @Test
    fun decode_rejectsFutureSchemaVersion() {
        assertThrows(DataBackupException::class.java) {
            DataBackupJson.decode(
                """
                {
                  "schemaVersion": 999,
                  "exportedAt": 1,
                  "appVersion": "future",
                  "settings": {},
                  "tasks": [],
                  "sessions": []
                }
                """.trimIndent()
            )
        }
    }
}
