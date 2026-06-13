package com.example.nashitimer.data.repository

import com.example.nashitimer.data.local.PomodoroDao
import com.example.nashitimer.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: PomodoroDao
) {
    fun allSessions(): Flow<List<PomodoroSession>> = dao.allSessions()

    suspend fun add(session: PomodoroSession): Boolean =
        dao.insertSessionAndUpdateTask(session)
}
