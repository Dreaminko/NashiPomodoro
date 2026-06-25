package com.dreaminko.nashipomodoro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: PomodoroSession): Long

    @Query(
        """
        UPDATE tasks
        SET pomodoroDone = pomodoroDone + 1,
            isCompleted = CASE
                WHEN pomodoroDone + 1 >= pomodoroGoal THEN 1
                ELSE isCompleted
            END
        WHERE id = :taskId
        """
    )
    suspend fun incrementTaskProgress(taskId: Long)

    @Transaction
    suspend fun insertSessionAndUpdateTask(session: PomodoroSession): Boolean {
        if (insertSession(session) == -1L) return false
        session.taskId?.let { incrementTaskProgress(it) }
        return true
    }

    @Query("SELECT * FROM pomodoro_sessions ORDER BY createdAt DESC")
    fun allSessions(): Flow<List<PomodoroSession>>

    @Query(
        """
        SELECT * FROM pomodoro_sessions
        WHERE completed = 1 AND phase = :phase
        ORDER BY createdAt DESC
        """
    )
    fun completedSessionsByPhase(phase: String): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY createdAt DESC")
    suspend fun allSessionsSnapshot(): List<PomodoroSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSessions(sessions: List<PomodoroSession>): List<Long>

    @Query(
        """
        SELECT COUNT(*) FROM pomodoro_sessions
        WHERE taskId = :taskId AND completed = 1 AND phase = :phase
        """
    )
    suspend fun completedSessionCountForTask(taskId: Long, phase: String): Int

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun taskSnapshot(taskId: Long): TaskItem?

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun tasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    suspend fun tasksSnapshot(): List<TaskItem>
}
