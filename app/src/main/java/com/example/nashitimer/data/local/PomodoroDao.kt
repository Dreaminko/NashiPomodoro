package com.example.nashitimer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TaskItem
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun tasks(): Flow<List<TaskItem>>
}
