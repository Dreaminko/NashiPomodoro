package com.example.nashitimer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nashitimer.domain.model.ThemeMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        NumberSetting("Focus", settings.focusDurationMin, "min", 5f..90f, viewModel::setFocus)
        NumberSetting("Short break", settings.shortBreakMin, "min", 1f..15f, viewModel::setShortBreak)
        NumberSetting("Long break", settings.longBreakMin, "min", 10f..30f, viewModel::setLongBreak)
        NumberSetting("Long break interval", settings.longBreakInterval, "rounds", 2f..8f, viewModel::setInterval)
        NumberSetting("Daily goal", settings.dailyGoal, "pomodoros", 1f..20f, viewModel::setDailyGoal)
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.themeMode == mode,
                    onClick = { viewModel.setTheme(mode) },
                    label = { Text(mode.name.lowercase().replace('_', ' ')) }
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Completion vibration")
            Switch(checked = settings.vibrationEnabled, onCheckedChange = viewModel::setVibration)
        }
    }
}

@Composable
private fun NumberSetting(
    label: String,
    value: Int,
    suffix: String,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text("$value $suffix", color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt().coerceAtLeast(0)
        )
    }
}
