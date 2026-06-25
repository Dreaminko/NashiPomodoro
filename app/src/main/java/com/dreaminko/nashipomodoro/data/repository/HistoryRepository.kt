package com.dreaminko.nashipomodoro.data.repository

import com.dreaminko.nashipomodoro.data.local.PomodoroDao
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: PomodoroDao
) {
    fun allSessions(): Flow<List<PomodoroSession>> = dao.allSessions()

    fun completedFocusSessions(): Flow<List<PomodoroSession>> =
        dao.completedSessionsByPhase(TimerPhase.FOCUS.name)

    suspend fun add(session: PomodoroSession): Boolean =
        dao.insertSessionAndUpdateTask(session)
}
