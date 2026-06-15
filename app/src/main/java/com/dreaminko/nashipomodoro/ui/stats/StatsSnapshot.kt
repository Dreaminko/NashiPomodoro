package com.dreaminko.nashipomodoro.ui.stats

import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailyFocus(
    val date: LocalDate,
    val durationMs: Long,
    val sessions: Int
)

data class StatsSnapshot(
    val todayFocusMs: Long = 0L,
    val todaySessions: Int = 0,
    val totalFocusMs: Long = 0L,
    val totalSessions: Int = 0,
    val activeDays: Int = 0,
    val weekAverageMs: Long = 0L,
    val monthAverageMs: Long = 0L,
    val week: List<DailyFocus> = emptyList(),
    val month: List<DailyFocus> = emptyList(),
    val timeOfDayFocusMs: List<Long> = List(4) { 0L },
    val heatmapCounts: List<Int> = List(84) { 0 }
)

fun buildStatsSnapshot(
    sessions: List<PomodoroSession>,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): StatsSnapshot {
    val completedFocusSessions = sessions.filter {
        it.completed && it.phase == TimerPhase.FOCUS.name
    }
    val sessionsByDate = completedFocusSessions.groupBy { it.localDate(zoneId) }

    fun dailyFocus(days: Int): List<DailyFocus> =
        (days - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val daySessions = sessionsByDate[date].orEmpty()
            DailyFocus(
                date = date,
                durationMs = daySessions.sumOf(PomodoroSession::durationMs),
                sessions = daySessions.size
            )
        }

    val week = dailyFocus(7)
    val month = dailyFocus(30)
    val heatmap = dailyFocus(84)
    val timeOfDayFocusMs = MutableList(4) { 0L }
    month.flatMap { sessionsByDate[it.date].orEmpty() }.forEach { session ->
        val hour = Instant.ofEpochMilli(session.startTime).atZone(zoneId).hour
        timeOfDayFocusMs[(hour / 6).coerceIn(0, 3)] += session.durationMs
    }

    val todaySessions = sessionsByDate[today].orEmpty()
    return StatsSnapshot(
        todayFocusMs = todaySessions.sumOf(PomodoroSession::durationMs),
        todaySessions = todaySessions.size,
        totalFocusMs = completedFocusSessions.sumOf(PomodoroSession::durationMs),
        totalSessions = completedFocusSessions.size,
        activeDays = sessionsByDate.size,
        weekAverageMs = week.sumOf(DailyFocus::durationMs) / week.size,
        monthAverageMs = month.sumOf(DailyFocus::durationMs) / month.size,
        week = week,
        month = month,
        timeOfDayFocusMs = timeOfDayFocusMs,
        heatmapCounts = heatmap.map(DailyFocus::sessions)
    )
}

private fun PomodoroSession.localDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDate()
