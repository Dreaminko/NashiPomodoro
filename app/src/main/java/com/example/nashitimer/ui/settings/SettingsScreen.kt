package com.example.nashitimer.ui.settings

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nashitimer.R
import com.example.nashitimer.domain.model.ThemeMode
import com.example.nashitimer.ui.components.PageTitle
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDebug: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val currentLanguage = LocalConfiguration.current.locales[0].language

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
                Column(Modifier.padding(start = 8.dp)) {
                    PageTitle(stringResource(R.string.settings_title))
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_timer_section)) }
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberSetting(
                        stringResource(R.string.settings_focus),
                        settings.focusDurationMin,
                        stringResource(R.string.unit_min),
                        5f..90f,
                        5,
                        viewModel::setFocus
                    )
                    NumberSetting(
                        stringResource(R.string.settings_short_break),
                        settings.shortBreakMin,
                        stringResource(R.string.unit_min),
                        5f..15f,
                        5,
                        viewModel::setShortBreak
                    )
                    NumberSetting(
                        stringResource(R.string.settings_long_break),
                        settings.longBreakMin,
                        stringResource(R.string.unit_min),
                        10f..30f,
                        5,
                        viewModel::setLongBreak
                    )
                    NumberSetting(
                        stringResource(R.string.settings_long_break_interval),
                        settings.longBreakInterval,
                        stringResource(R.string.unit_rounds),
                        2f..8f,
                        onChange = viewModel::setInterval
                    )
                    NumberSetting(
                        stringResource(R.string.settings_daily_goal),
                        settings.dailyGoal,
                        stringResource(R.string.unit_sessions),
                        1f..20f,
                        onChange = viewModel::setDailyGoal
                    )
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_appearance_section)) }
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.settings_theme),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setTheme(mode) },
                                label = { Text(mode.displayName()) }
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageOption.entries.forEach { language ->
                            FilterChip(
                                selected = currentLanguage == language.languageCode,
                                onClick = {
                                    context.getSystemService(LocaleManager::class.java)
                                        .applicationLocales =
                                        LocaleList.forLanguageTags(language.languageTag)
                                },
                                label = { Text(stringResource(language.labelRes)) }
                            )
                        }
                    }
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_feedback_section)) }
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_completion_vibration),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.settings_completion_vibration_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = viewModel::setVibration
                    )
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_developer_section)) }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDebug),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_debug),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (settings.debugModeEnabled) {
                                stringResource(
                                    R.string.settings_debug_enabled,
                                    settings.debugFocusDurationSec
                                )
                            } else {
                                stringResource(R.string.settings_debug_description)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.settings_open_debug),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun NumberSetting(
    label: String,
    value: Int,
    suffix: String,
    range: ClosedFloatingPointRange<Float>,
    step: Int = 1,
    onChange: (Int) -> Unit
) {
    val intervalCount = ((range.endInclusive - range.start) / step).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                "$value $suffix",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value.toFloat().coerceIn(range.start, range.endInclusive),
            onValueChange = {
                val steppedValue =
                    ((it - range.start) / step).roundToInt() * step + range.start.toInt()
                onChange(steppedValue)
            },
            valueRange = range,
            steps = (intervalCount - 1).coerceAtLeast(0)
        )
    }
}

@Composable
private fun ThemeMode.displayName(): String = stringResource(
    when (this) {
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
        ThemeMode.FOLLOW_SYSTEM -> R.string.theme_system
    }
)

private enum class LanguageOption(
    val languageCode: String,
    val languageTag: String,
    val labelRes: Int
) {
    ENGLISH("en", "en", R.string.language_english),
    SIMPLIFIED_CHINESE("zh", "zh-CN", R.string.language_simplified_chinese)
}
