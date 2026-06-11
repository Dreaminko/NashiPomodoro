package com.example.nashitimer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession)

    @Query("SELECT * FROM pomodoro_sessions WHERE createdAt >= :since ORDER BY createdAt DESC")
    fun sessionsSince(since: Long): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY createdAt DESC")
    fun allSessions(): Flow<List<PomodoroSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun tasks(): Flow<List<TaskItem>>
}
