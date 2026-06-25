package com.dreaminko.nashipomodoro.ui.settings

import android.app.LocaleManager
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreaminko.nashipomodoro.BuildConfig
import com.dreaminko.nashipomodoro.R
import com.dreaminko.nashipomodoro.core.update.AppUpdate
import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection
import com.dreaminko.nashipomodoro.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenReminder: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenDataBackup: () -> Unit,
    onOpenDebug: () -> Unit,
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()

    SettingsPage(
        title = stringResource(R.string.settings_title),
        onBack = onBack
    ) {
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
                icon = Icons.Rounded.Storage,
                title = stringResource(R.string.settings_data_section),
                description = stringResource(R.string.settings_data_category_description),
                onClick = onOpenDataBackup
            )
        }
        item {
            CategorySetting(
                icon = Icons.Rounded.SystemUpdate,
                title = stringResource(R.string.settings_check_update),
                description = stringResource(
                    R.string.settings_check_update_description,
                    BuildConfig.VERSION_NAME
                ),
                enabled = updateUiState != UpdateUiState.Checking,
                onClick = updateViewModel::checkForUpdate
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

    UpdateResultDialog(
        state = updateUiState,
        onDismiss = updateViewModel::dismissResult,
        onDownload = updateViewModel::downloadUpdate
    )
}

@Composable
private fun UpdateResultDialog(
    state: UpdateUiState,
    onDismiss: () -> Unit,
    onDownload: (AppUpdate) -> Unit
) {
    when (state) {
        UpdateUiState.Idle -> Unit
        UpdateUiState.Checking -> AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            icon = { CircularProgressIndicator(modifier = Modifier.size(32.dp)) },
            title = { Text(stringResource(R.string.update_checking_title)) },
            text = { Text(stringResource(R.string.update_checking_description)) }
        )
        UpdateUiState.UpToDate -> UpdateMessageDialog(
            title = stringResource(R.string.update_up_to_date_title),
            message = stringResource(
                R.string.update_up_to_date_description,
                BuildConfig.VERSION_NAME
            ),
            onDismiss = onDismiss
        )
        is UpdateUiState.Available -> AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { onDownload(state.update) }) {
                    Text(stringResource(R.string.update_download))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            title = {
                Text(stringResource(R.string.update_available_title, state.update.versionName))
            },
            text = {
                Text(
                    state.update.releaseNotes
                        .ifBlank { stringResource(R.string.update_no_release_notes) }
                        .take(MAX_RELEASE_NOTES_LENGTH)
                )
            }
        )
        UpdateUiState.DownloadStarted -> UpdateMessageDialog(
            title = stringResource(R.string.update_download_started_title),
            message = stringResource(R.string.update_download_started_description),
            onDismiss = onDismiss
        )
        UpdateUiState.Error -> UpdateMessageDialog(
            title = stringResource(R.string.update_error_title),
            message = stringResource(R.string.update_error_description),
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun UpdateMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(title) },
        text = { Text(message) }
    )
}

@Composable
fun TimerSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsPage(
        title = stringResource(R.string.settings_timer_section),
        onBack = onBack
    ) {
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val availableGlyphChannels = viewModel.availableGlyphChannels

    SettingsPage(
        title = stringResource(R.string.settings_reminder_section),
        onBack = onBack
    ) {
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
                SettingDivider()
                NumberSetting(
                    label = stringResource(R.string.settings_vibration_intensity),
                    description = stringResource(R.string.settings_vibration_intensity_description),
                    value = settings.vibrationIntensity,
                    suffix = stringResource(R.string.unit_percent),
                    range = 10f..100f,
                    step = 10,
                    enabled = settings.vibrationEnabled,
                    onPreview = viewModel::previewVibrationIntensity,
                    onChange = viewModel::setVibrationIntensity
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_glyph_section),
                description = stringResource(R.string.settings_glyph_section_description)
            )
        }
        item {
            SettingsCard {
                GlyphProgressSetting(
                    label = stringResource(R.string.settings_glyph_progress),
                    description = stringResource(R.string.settings_glyph_progress_description),
                    enabled = settings.glyphProgressEnabled,
                    selectedChannel = settings.glyphProgressChannel,
                    selectedDirection = settings.glyphProgressDirection,
                    availableChannels = availableGlyphChannels,
                    onEnabledChange = viewModel::setGlyphProgress,
                    onChannelChange = viewModel::setGlyphProgressChannel,
                    onDirectionChange = viewModel::setGlyphProgressDirection
                )
                SettingDivider()
                GlyphProgressSetting(
                    label = stringResource(R.string.settings_glyph_short_break_progress),
                    description = stringResource(
                        R.string.settings_glyph_short_break_progress_description
                    ),
                    enabled = settings.glyphShortBreakProgressEnabled,
                    selectedChannel = settings.glyphShortBreakProgressChannel,
                    selectedDirection = settings.glyphShortBreakProgressDirection,
                    availableChannels = availableGlyphChannels,
                    onEnabledChange = viewModel::setGlyphShortBreakProgress,
                    onChannelChange = viewModel::setGlyphShortBreakProgressChannel,
                    onDirectionChange = viewModel::setGlyphShortBreakProgressDirection
                )
                SettingDivider()
                GlyphProgressSetting(
                    label = stringResource(R.string.settings_glyph_long_break_progress),
                    description = stringResource(
                        R.string.settings_glyph_long_break_progress_description
                    ),
                    enabled = settings.glyphLongBreakProgressEnabled,
                    selectedChannel = settings.glyphLongBreakProgressChannel,
                    selectedDirection = settings.glyphLongBreakProgressDirection,
                    availableChannels = availableGlyphChannels,
                    onEnabledChange = viewModel::setGlyphLongBreakProgress,
                    onChannelChange = viewModel::setGlyphLongBreakProgressChannel,
                    onDirectionChange = viewModel::setGlyphLongBreakProgressDirection
                )
                SettingDivider()
                ToggleSetting(
                    label = stringResource(R.string.settings_glyph_completion_flash),
                    description = stringResource(
                        R.string.settings_glyph_completion_flash_description
                    ),
                    checked = settings.glyphCompletionFlashEnabled,
                    onCheckedChange = viewModel::setGlyphCompletionFlash
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
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

    SettingsPage(
        title = stringResource(R.string.settings_appearance_section),
        onBack = onBack
    ) {
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
                            label = { Text(mode.displayName()) },
                            leadingIcon = if (settings.themeMode == mode) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
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

private const val MAX_RELEASE_NOTES_LENGTH = 2_000

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LanguageDropdownSetting(
    label: String,
    description: String,
    selected: LanguageOption,
    onSelect: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDisplayName = selected.displayName()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingText(label, description)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedDisplayName,
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                LanguageOption.entries.forEach { language ->
                    val isSelected = language == selected
                    val displayName = language.displayName()
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            expanded = false
                            onSelect(language)
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlyphProgressSetting(
    label: String,
    description: String,
    enabled: Boolean,
    selectedChannel: GlyphChannel,
    selectedDirection: GlyphProgressDirection,
    availableChannels: List<GlyphChannel>,
    onEnabledChange: (Boolean) -> Unit,
    onChannelChange: (GlyphChannel) -> Unit,
    onDirectionChange: (GlyphProgressDirection) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToggleSetting(
            label = label,
            description = description,
            checked = enabled,
            onCheckedChange = onEnabledChange
        )
        if (enabled) {
            Text(
                text = stringResource(R.string.settings_glyph_light_strip),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableChannels.forEach { channel ->
                    FilterChip(
                        selected = selectedChannel == channel,
                        onClick = { onChannelChange(channel) },
                        label = { Text(channel.displayName()) },
                        leadingIcon = if (selectedChannel == channel) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
            Text(
                text = stringResource(R.string.settings_glyph_flow_direction),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlyphProgressDirection.entries.forEach { direction ->
                    FilterChip(
                        selected = selectedDirection == direction,
                        onClick = { onDirectionChange(direction) },
                        label = { Text(direction.displayName()) },
                        leadingIcon = if (selectedDirection == direction) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlyphChannel.displayName(): String =
    if (this == GlyphChannel.AUTO) {
        stringResource(R.string.settings_glyph_light_strip_auto)
    } else {
        name
    }

@Composable
private fun GlyphProgressDirection.displayName(): String = stringResource(
    when (this) {
        GlyphProgressDirection.FORWARD -> R.string.settings_glyph_flow_forward
        GlyphProgressDirection.REVERSE -> R.string.settings_glyph_flow_reverse
    }
)

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
    val nativeName: String?
) {
    FOLLOW_SYSTEM(null, null, null),
    ENGLISH("en", "en", "English"),
    SIMPLIFIED_CHINESE("zh", "zh-CN", "简体中文"),
    JAPANESE("ja", "ja", "日本語")
}

@Composable
private fun LanguageOption.displayName(): String {
    nativeName?.let { return it }

    val context = LocalContext.current
    val systemLocales = context.getSystemService(LocaleManager::class.java).systemLocales
    val systemConfiguration = Configuration(LocalConfiguration.current).apply {
        setLocales(systemLocales)
    }
    return context.createConfigurationContext(systemConfiguration)
        .getString(R.string.language_system)
}
