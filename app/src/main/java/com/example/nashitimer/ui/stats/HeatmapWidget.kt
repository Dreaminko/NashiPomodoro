package com.example.nashitimer.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nashitimer.R
import java.time.LocalDate

private val CellSize = 14.dp
private val CellSpacing = 4.dp

@Composable
fun HeatmapWidget(counts: List<Int>, modifier: Modifier = Modifier) {
    val days = counts.takeLast(84).let { recent ->
        List(84 - recent.size) { 0 } + recent
    }
    val startDate = LocalDate.now().minusDays((days.size - 1).toLong())
    val leadingDays = startDate.dayOfWeek.value % 7
    val datedCells = List<Int?>(leadingDays) { null } + days
    val trailingDays = (7 - datedCells.size % 7) % 7
    val calendarCells = datedCells + List<Int?>(trailingDays) { null }
    val levels = contributionColors()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeekdayLabels()
            Row(horizontalArrangement = Arrangement.spacedBy(CellSpacing)) {
                calendarCells.chunked(7).forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(CellSpacing)) {
                        week.forEach { count ->
                            ContributionCell(
                                color = count?.let { levels[colorLevel(it)] } ?: Color.Transparent
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = stringResource(R.string.heatmap_less),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            levels.forEach { color -> ContributionCell(color) }
            Text(
                text = stringResource(R.string.heatmap_more),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeekdayLabels() {
    Column(
        modifier = Modifier.width(28.dp),
        verticalArrangement = Arrangement.spacedBy(CellSpacing)
    ) {
        listOf(
            "",
            stringResource(R.string.weekday_mon),
            "",
            stringResource(R.string.weekday_wed),
            "",
            stringResource(R.string.weekday_fri),
            ""
        ).forEach { label ->
            Box(
                modifier = Modifier.height(CellSize),
                contentAlignment = Alignment.CenterStart
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ContributionCell(color: Color) {
    Box(
        Modifier
            .size(CellSize)
            .background(color, RoundedCornerShape(3.dp))
    )
}

@Composable
private fun contributionColors(): List<Color> {
    val active = MaterialTheme.colorScheme.primary
    return listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        active.copy(alpha = 0.28f),
        active.copy(alpha = 0.5f),
        active.copy(alpha = 0.72f),
        active
    )
}

private fun colorLevel(count: Int): Int = when {
    count <= 0 -> 0
    count == 1 -> 1
    count <= 3 -> 2
    count <= 5 -> 3
    else -> 4
}
