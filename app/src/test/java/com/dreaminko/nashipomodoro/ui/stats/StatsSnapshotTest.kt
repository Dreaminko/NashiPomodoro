package com.dreaminko.nashipomodoro.ui.stats

import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StatsSnapshotTest {
    private val zoneId = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 6, 12)

    @Test
    fun aggregatesCompletedFocusSessionsAcrossRanges() {
        val sessions = listOf(
            session(today, hour = 9, durationMinutes = 25),
            session(today, hour = 14, durationMinutes = 50),
            session(today.minusDays(1), hour = 20, durationMinutes = 25),
            session(today.minusDays(8), hour = 2, durationMinutes = 30),
            session(today, hour = 10, durationMinutes = 25, completed = false),
            session(today, hour = 10, durationMinutes = 25, phase = TimerPhase.SHORT_BREAK)
        )

        val result = buildStatsSnapshot(sessions, today, zoneId)

        assertEquals(75 * 60_000L, result.todayFocusMs)
        assertEquals(2, result.todaySessions)
        assertEquals(130 * 60_000L, result.totalFocusMs)
        assertEquals(4, result.totalSessions)
        assertEquals(3, result.activeDays)
        assertEquals(100 * 60_000L / 7, result.weekAverageMs)
        assertEquals(130 * 60_000L / 30, result.monthAverageMs)
        assertEquals(listOf(30L, 25L, 50L, 25L).map { it * 60_000L }, result.timeOfDayFocusMs)
        assertEquals(2, result.heatmapCounts.last())
    }

    @Test
    fun crossMidnightSession_isAttributedToItsStartDate() {
        val start = today.minusDays(1).atTime(23, 50)
            .atZone(zoneId).toInstant().toEpochMilli()
        val session = PomodoroSession(
            startTime = start,
            endTime = start + 25 * 60_000L,
            phase = TimerPhase.FOCUS.name,
            durationMs = 25 * 60_000L,
            completed = true,
            createdAt = start + 25 * 60_000L
        )

        val result = buildStatsSnapshot(listOf(session), today, zoneId)

        assertEquals(0L, result.todayFocusMs)
        assertEquals(25 * 60_000L, result.week[result.week.lastIndex - 1].durationMs)
    }

    private fun session(
        date: LocalDate,
        hour: Int,
        durationMinutes: Long,
        completed: Boolean = true,
        phase: TimerPhase = TimerPhase.FOCUS
    ): PomodoroSession {
        val start = date.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
        val durationMs = durationMinutes * 60_000L
        return PomodoroSession(
            startTime = start,
            endTime = start + durationMs,
            phase = phase.name,
            durationMs = durationMs,
            completed = completed,
            createdAt = start + durationMs
        )
    }
}
