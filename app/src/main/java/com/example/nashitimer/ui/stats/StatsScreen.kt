package com.example.nashitimer.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nashitimer.R
import com.example.nashitimer.ui.components.PageTitle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val sessions by viewModel.sessions.collectAsState()
    val today = LocalDate.now()
    val todaySessions = sessions.filter {
        Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate() == today
    }
    val totalMinutes = sessions.sumOf { it.durationMs } / 60000
    val counts = (83 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        sessions.count {
            Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate() == day
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 24.dp,
            vertical = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            PageTitle(stringResource(R.string.nav_insights))
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    stringResource(R.string.stats_today),
                    todaySessions.size.toString(),
                    stringResource(R.string.unit_sessions),
                    Modifier.weight(1f)
                )
                StatCard(
                    stringResource(R.string.stats_all_time),
                    totalMinutes.toString(),
                    stringResource(R.string.unit_minutes),
                    Modifier.weight(1f)
                )
            }
        }
        item {
            Surface(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            stringResource(R.string.stats_last_12_weeks),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            stringResource(R.string.stats_activity_summary),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HeatmapWidget(counts, Modifier.fillMaxWidth())
                }
            }
        }
        if (sessions.isEmpty()) {
            item {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

@Composable
private fun StatCard(
    title: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(suffix, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
