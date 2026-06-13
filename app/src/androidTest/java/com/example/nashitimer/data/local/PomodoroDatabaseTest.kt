package com.example.nashitimer.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nashitimer.di.MIGRATION_2_3
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TaskItem
import com.example.nashitimer.domain.model.TimerPhase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PomodoroDatabaseTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PomodoroDatabase::class.java
    )

    private var database: PomodoroDatabase? = null

    @After
    fun tearDown() {
        database?.close()
    }

    @Test
    fun migration2To3_removesDuplicatesAndAddsUniqueConstraint() {
        migrationHelper.createDatabase(TEST_DATABASE, 2).apply {
            repeat(2) {
                execSQL(
                    """
                    INSERT INTO pomodoro_sessions (
                        startTime, endTime, phase, durationMs, completed, taskId, tag, createdAt
                    ) VALUES (1000, 2000, 'FOCUS', 1000, 1, NULL, 'Focus', 2000)
                    """.trimIndent()
                )
            }
            close()
        }

        val migrated = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            3,
            true,
            MIGRATION_2_3
        )
        migrated.query("SELECT COUNT(*) FROM pomodoro_sessions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun completedSession_updatesTaskExactlyOnce() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PomodoroDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = database!!.pomodoroDao()
        dao.upsertTask(
            TaskItem(
                id = 42,
                title = "Ship review",
                pomodoroGoal = 1,
                createdAt = 1_000L
            )
        )
        val session = PomodoroSession(
            startTime = 1_000L,
            endTime = 2_000L,
            phase = TimerPhase.FOCUS.name,
            durationMs = 1_000L,
            completed = true,
            taskId = 42,
            createdAt = 2_000L
        )

        assertTrue(dao.insertSessionAndUpdateTask(session))
        assertFalse(dao.insertSessionAndUpdateTask(session))

        val task = dao.tasks().first().single()
        assertEquals(1, task.pomodoroDone)
        assertTrue(task.isCompleted)
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
