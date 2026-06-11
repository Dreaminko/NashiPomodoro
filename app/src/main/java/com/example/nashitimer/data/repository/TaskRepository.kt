package com.example.nashitimer.data.repository

import com.example.nashitimer.data.local.PomodoroDao
import com.example.nashitimer.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val dao: PomodoroDao
) {
    val tasks: Flow<List<TaskItem>> = dao.tasks()
    suspend fun add(title: String) = dao.upsertTask(TaskItem(title = title, createdAt = System.currentTimeMillis()))
    suspend fun update(task: TaskItem) = dao.updateTask(task)
    suspend fun delete(task: TaskItem) = dao.deleteTask(task)
}
