package com.example.nashitimer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nashitimer.data.repository.SettingsRepository
import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings()
    )

    fun setFocus(value: Int) = update { copy(focusDurationMin = value.coerceIn(5, 90)) }
    fun setShortBreak(value: Int) = update { copy(shortBreakMin = value.coerceIn(1, 15)) }
    fun setLongBreak(value: Int) = update { copy(longBreakMin = value.coerceIn(10, 30)) }
    fun setInterval(value: Int) = update { copy(longBreakInterval = value.coerceIn(2, 8)) }
    fun setDailyGoal(value: Int) = update { copy(dailyGoal = value.coerceIn(1, 20)) }
    fun setTheme(value: ThemeMode) = update { copy(themeMode = value) }
    fun setVibration(value: Boolean) = update { copy(vibrationEnabled = value) }

    private fun update(block: AppSettings.() -> AppSettings) {
        viewModelScope.launch { repository.update(block) }
    }
}
