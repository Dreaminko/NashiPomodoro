package com.example.nashitimer.ui.settings

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    onOpenTimer: () -> Unit,
    onOpenReminder: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenDebug: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SettingsHeader(stringResource(R.string.settings_title), onBack) }
        item {
            CategorySetting(
                icon = Icons.Rounded.Timer,
                title = stringResource(R.string.settings_timer_section),
                description = stringResource(R.string.settings_timer_category_description),
                onClick = onOpenTimer
            )
        }
        item {
            CategorySetting(
                icon = Icons.Rounded.Notifications,
                title = stringResource(R.string.settings_reminder_section),
                description = stringResource(R.string.settings_reminder_category_description),
                onClick = onOpenReminder
            )
        }
        item {
            CategorySetting(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.settings_appearance_section),
                description = stringResource(R.string.settings_appearance_category_description),
                onClick = onOpenAppearance
            )
        }
        item {
            CategorySetting(
                icon = Icons.Rounded.BugReport,
                title = stringResource(R.string.settings_developer_section),
                description = stringResource(R.string.settings_debug_description),
                onClick = onOpenDebug
            )
        }
    }
}

@Composable
fun TimerSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SettingsHeader(stringResource(R.string.settings_timer_section), onBack)
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_focus_section),
                description = stringResource(R.string.settings_focus_section_description)
            )
        }
        item {
            SettingsCard {
                NumberSetting(
                    label = stringResource(R.string.settings_focus),
                    description = stringResource(R.string.settings_focus_description),
                    value = settings.focusDurationMin,
                    suffix = stringResource(R.string.unit_min),
                    range = 5f..90f,
                    step = 5,
                    onChange = viewModel::setFocus
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_breaks_section),
                description = stringResource(R.string.settings_breaks_section_description)
            )
        }
        item {
            SettingsCard {
                NumberSetting(
                    label = stringResource(R.string.settings_short_break),
                    description = stringResource(R.string.settings_short_break_description),
                    value = settings.shortBreakMin,
                    suffix = stringResource(R.string.unit_min),
                    range = 5f..15f,
                    step = 5,
                    onChange = viewModel::setShortBreak
                )
                SettingDivider()
                NumberSetting(
                    label = stringResource(R.string.settings_long_break),
                    description = stringResource(R.string.settings_long_break_description),
                    value = settings.longBreakMin,
                    suffix = stringResource(R.string.unit_min),
                    range = 10f..30f,
                    step = 5,
                    onChange = viewModel::setLongBreak
                )
                SettingDivider()
                StepperSetting(
                    label = stringResource(R.string.settings_long_break_interval),
                    description = stringResource(R.string.settings_long_break_interval_description),
                    value = settings.longBreakInterval,
                    suffix = stringResource(R.string.unit_rounds),
                    range = 2..8,
                    onChange = viewModel::setInterval
                )
                SettingDivider()
                StepperSetting(
                    label = stringResource(R.string.settings_daily_goal),
                    description = stringResource(R.string.settings_daily_goal_description),
                    value = settings.dailyGoal,
                    suffix = stringResource(R.string.unit_sessions),
                    range = 1..20,
                    onChange = viewModel::setDailyGoal
                )
            }
        }

    }
}

@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SettingsHeader(stringResource(R.string.settings_reminder_section), onBack) }
        item {
            SectionHeader(
                title = stringResource(R.string.settings_completion_section),
                description = stringResource(R.string.settings_completion_section_description)
            )
        }
        item {
            SettingsCard {
                ToggleSetting(
                    label = stringResource(R.string.settings_completion_vibration),
                    description = stringResource(
                        R.string.settings_completion_vibration_description
                    ),
                    checked = settings.vibrationEnabled,
                    onCheckedChange = viewModel::setVibration
                )
            }
        }
    }
}

@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val localeManager = context.getSystemService(LocaleManager::class.java)
    val applicationLocales = localeManager.applicationLocales
    val selectedLanguage = if (applicationLocales.isEmpty) {
        LanguageOption.FOLLOW_SYSTEM
    } else {
        LanguageOption.entries.firstOrNull {
            it.languageCode == applicationLocales[0].language
        } ?: LanguageOption.FOLLOW_SYSTEM
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SettingsHeader(stringResource(R.string.settings_appearance_section), onBack) }
        item {
            SettingsCard {
                ChoiceSetting(
                    label = stringResource(R.string.settings_theme),
                    description = stringResource(R.string.settings_theme_description)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                            label = { Text(mode.displayName()) }
                        )
                    }
                }
                SettingDivider()
                LanguageDropdownSetting(
                    label = stringResource(R.string.settings_language),
                    description = stringResource(R.string.settings_language_description),
                    selected = selectedLanguage,
                    onSelect = { language ->
                        localeManager.applicationLocales = if (language.languageTag == null) {
                            LocaleList.getEmptyLocaleList()
                        } else {
                            LocaleList.forLanguageTags(language.languageTag)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.action_back)
            )
        }
        Column(Modifier.padding(start = 12.dp)) {
            PageTitle(title)
        }
    }
}

@Composable
private fun CategorySetting(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp)
                )
            }
            SettingText(title, description, Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, description: String) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SettingText(label: String, description: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NumberSetting(
    label: String,
    description: String,
    value: Int,
    suffix: String,
    range: ClosedFloatingPointRange<Float>,
    step: Int,
    onChange: (Int) -> Unit
) {
    val intervalCount = ((range.endInclusive - range.start) / step).toInt()
    var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            SettingText(label, description, Modifier.weight(1f))
            Text(
                text = "${sliderValue.roundToInt()} $suffix",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = sliderValue.coerceIn(range.start, range.endInclusive),
            onValueChange = {
                sliderValue =
                    (((it - range.start) / step).roundToInt() * step + range.start).coerceIn(
                        range.start,
                        range.endInclusive
                    )
            },
            onValueChangeFinished = { onChange(sliderValue.roundToInt()) },
            valueRange = range,
            steps = (intervalCount - 1).coerceAtLeast(0)
        )
    }
}

@Composable
private fun StepperSetting(
    label: String,
    description: String,
    value: Int,
    suffix: String,
    range: IntRange,
    onChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingText(label, description)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 6.dp,
                alignment = Alignment.End
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = { onChange(value - 1) },
                enabled = value > range.first,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = stringResource(R.string.action_remove)
                )
            }
            Text(
                text = "$value $suffix",
                modifier = Modifier.padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedIconButton(
                onClick = { onChange(value + 1) },
                enabled = value < range.last,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.action_add)
                )
            }
        }
    }
}

@Composable
private fun ChoiceSetting(
    label: String,
    description: String,
    choices: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingText(label, description)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices()
        }
    }
}

@Composable
private fun LanguageDropdownSetting(
    label: String,
    description: String,
    selected: LanguageOption,
    onSelect: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingText(label, description)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(selected.labelRes))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 200.dp, max = 280.dp)
            ) {
                LanguageOption.entries.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(stringResource(language.labelRes)) },
                        onClick = {
                            expanded = false
                            onSelect(language)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleSetting(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(label, description, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
    val languageCode: String?,
    val languageTag: String?,
    val labelRes: Int
) {
    FOLLOW_SYSTEM(null, null, R.string.language_system),
    ENGLISH("en", "en", R.string.language_english),
    SIMPLIFIED_CHINESE("zh", "zh-CN", R.string.language_simplified_chinese)
}
