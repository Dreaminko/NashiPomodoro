package com.example.nashitimer.data.repository

import com.example.nashitimer.data.local.PomodoroDao
import com.example.nashitimer.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: PomodoroDao
) {
    fun allSessions(): Flow<List<PomodoroSession>> = dao.allSessions()

    fun todaySessions(): Flow<List<PomodoroSession>> {
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.sessionsSince(start)
    }

    suspend fun add(session: PomodoroSession) = dao.insertSession(session)
}
