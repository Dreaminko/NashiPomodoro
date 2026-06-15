package com.dreaminko.nashipomodoro.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreaminko.nashipomodoro.R
import com.dreaminko.nashipomodoro.ui.components.PageTitle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

private enum class TrendPeriod { WEEK, MONTH }

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    var trendPeriod by remember { mutableStateOf(TrendPeriod.WEEK) }
    val trend = if (trendPeriod == TrendPeriod.WEEK) stats.week else stats.month
    val average = if (trendPeriod == TrendPeriod.WEEK) {
        stats.weekAverageMs
    } else {
        stats.monthAverageMs
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(
            text = stringResource(R.string.nav_insights),
            modifier = Modifier.padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 14.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeading(stringResource(R.string.stats_today))
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HighlightCard(
                        title = stringResource(R.string.stats_focus_time),
                        value = formatDuration(stats.todayFocusMs),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    HighlightCard(
                        title = stringResource(R.string.stats_completed_sessions),
                        value = stats.todaySessions.toString(),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStat(
                            label = stringResource(R.string.stats_all_time),
                            value = formatDuration(stats.totalFocusMs),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStat(
                            label = stringResource(R.string.stats_total_sessions),
                            value = stats.totalSessions.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStat(
                            label = stringResource(R.string.stats_active_days),
                            value = stats.activeDays.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                StatsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            SectionHeading(stringResource(R.string.stats_focus_trend))
                            Text(
                                text = stringResource(
                                    R.string.stats_daily_average,
                                    formatDuration(average)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = trendPeriod == TrendPeriod.WEEK,
                                onClick = { trendPeriod = TrendPeriod.WEEK },
                                label = { Text(stringResource(R.string.stats_week)) }
                            )
                            FilterChip(
                                selected = trendPeriod == TrendPeriod.MONTH,
                                onClick = { trendPeriod = TrendPeriod.MONTH },
                                label = { Text(stringResource(R.string.stats_month)) }
                            )
                        }
                    }
                    FocusTrendChart(
                        points = trend,
                        showEveryLabel = trendPeriod == TrendPeriod.WEEK
                    )
                }
            }

            item {
                val productiveIndex = stats.timeOfDayFocusMs.indices.maxByOrNull {
                    stats.timeOfDayFocusMs[it]
                } ?: 0
                val labels = timePeriodLabels()
                StatsCard {
                    SectionHeading(stringResource(R.string.stats_productivity))
                    Text(
                        text = if (stats.timeOfDayFocusMs.any { it > 0L }) {
                            stringResource(
                                R.string.stats_most_productive,
                                labels[productiveIndex]
                            )
                        } else {
                            stringResource(R.string.stats_productivity_description)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TimeOfDayChart(stats.timeOfDayFocusMs, labels)
                }
            }

            item {
                StatsCard {
                    SectionHeading(stringResource(R.string.stats_last_12_weeks))
                    Text(
                        stringResource(R.string.stats_activity_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HeatmapWidget(stats.heatmapCounts, Modifier.fillMaxWidth())
                }
            }

            if (stats.totalSessions == 0) {
                item {
                    Surface(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                stringResource(R.string.stats_empty_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                stringResource(R.string.stats_empty_description),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightCard(
    title: String,
    value: String,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier, color = color, shape = MaterialTheme.shapes.large) {
        Column(
            Modifier
                .heightIn(min = 112.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun StatsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun FocusTrendChart(points: List<DailyFocus>, showEveryLabel: Boolean) {
    val maxDuration = max(points.maxOfOrNull(DailyFocus::durationMs) ?: 0L, 1L)
    val activeColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp),
        horizontalArrangement = Arrangement.spacedBy(if (showEveryLabel) 8.dp else 3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEachIndexed { index, point ->
            val fraction = point.durationMs.toFloat() / maxDuration
            val showLabel = showEveryLabel || index == 0 || index == points.lastIndex ||
                index % 5 == 0
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(if (showEveryLabel) 0.72f else 0.64f)
                            .fillMaxHeight(if (point.durationMs == 0L) 0.025f else fraction)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(if (point.durationMs == 0L) emptyColor else activeColor)
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = when {
                        !showLabel -> ""
                        showEveryLabel -> point.date.dayOfWeek.getDisplayName(
                            TextStyle.NARROW,
                            Locale.getDefault()
                        )
                        else -> point.date.dayOfMonth.toString()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TimeOfDayChart(values: List<Long>, labels: List<String>) {
    val maxValue = max(values.maxOrNull() ?: 0L, 1L)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        values.forEachIndexed { index, value ->
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDuration(value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    if (value > 0L) {
                        Box(
                            Modifier
                                .fillMaxWidth(value.toFloat() / maxValue)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun timePeriodLabels(): List<String> = listOf(
    stringResource(R.string.stats_time_night),
    stringResource(R.string.stats_time_morning),
    stringResource(R.string.stats_time_afternoon),
    stringResource(R.string.stats_time_evening)
)

@Composable
private fun formatDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.stats_duration_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.stats_duration_minutes, minutes)
    }
}
