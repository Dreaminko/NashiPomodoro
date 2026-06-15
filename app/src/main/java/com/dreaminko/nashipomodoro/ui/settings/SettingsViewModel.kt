package com.dreaminko.nashipomodoro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreaminko.nashipomodoro.core.glyph.GlyphDeviceAdapter
import com.dreaminko.nashipomodoro.core.haptics.VibrationController
import com.dreaminko.nashipomodoro.data.repository.SettingsRepository
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection
import com.dreaminko.nashipomodoro.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val vibrationController: VibrationController,
    private val repository: SettingsRepository,
    adapter: GlyphDeviceAdapter
) : ViewModel() {
    val availableGlyphChannels: List<GlyphChannel> =
        listOf(GlyphChannel.AUTO) + adapter.profile.availableProgressChannels

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings()
    )

    fun setFocus(value: Int) = update { copy(focusDurationMin = value.snapToStep(5, 90, 5)) }
    fun setShortBreak(value: Int) = update { copy(shortBreakMin = value.snapToStep(5, 15, 5)) }
    fun setLongBreak(value: Int) = update { copy(longBreakMin = value.snapToStep(10, 30, 5)) }
    fun setInterval(value: Int) = update { copy(longBreakInterval = value.coerceIn(2, 8)) }
    fun setDailyGoal(value: Int) = update { copy(dailyGoal = value.coerceIn(1, 20)) }
    fun setTheme(value: ThemeMode) = update { copy(themeMode = value) }
    fun setVibration(value: Boolean) = update { copy(vibrationEnabled = value) }
    fun setVibrationIntensity(value: Int) =
        update { copy(vibrationIntensity = value.snapToStep(10, 100, 10)) }
    fun setGlyphProgress(value: Boolean) = update { copy(glyphProgressEnabled = value) }
    fun setGlyphProgressChannel(value: GlyphChannel) =
        update { copy(glyphProgressChannel = value) }
    fun setGlyphProgressDirection(value: GlyphProgressDirection) =
        update { copy(glyphProgressDirection = value) }
    fun setGlyphShortBreakProgress(value: Boolean) =
        update { copy(glyphShortBreakProgressEnabled = value) }
    fun setGlyphShortBreakProgressChannel(value: GlyphChannel) =
        update { copy(glyphShortBreakProgressChannel = value) }
    fun setGlyphShortBreakProgressDirection(value: GlyphProgressDirection) =
        update { copy(glyphShortBreakProgressDirection = value) }
    fun setGlyphLongBreakProgress(value: Boolean) =
        update { copy(glyphLongBreakProgressEnabled = value) }
    fun setGlyphLongBreakProgressChannel(value: GlyphChannel) =
        update { copy(glyphLongBreakProgressChannel = value) }
    fun setGlyphLongBreakProgressDirection(value: GlyphProgressDirection) =
        update { copy(glyphLongBreakProgressDirection = value) }
    fun setGlyphCompletionFlash(value: Boolean) =
        update { copy(glyphCompletionFlashEnabled = value) }

    fun previewVibrationIntensity(value: Int) {
        val intensity = value.snapToStep(10, 100, 10)
        vibrationController.previewIntensity(
            AppSettings(vibrationIntensity = intensity).vibrationAmplitude
        )
    }

    fun setDebugMode(value: Boolean) = update { copy(debugModeEnabled = value) }
    fun setDebugFocusDurationSeconds(value: Int) =
        update { copy(debugFocusDurationSec = value.coerceIn(1, 3_600)) }

    private fun update(block: AppSettings.() -> AppSettings) {
        viewModelScope.launch { repository.update(block) }
    }
}

internal fun Int.snapToStep(min: Int, max: Int, step: Int): Int {
    val clamped = coerceIn(min, max)
    return min + ((clamped - min + step / 2) / step) * step
}
