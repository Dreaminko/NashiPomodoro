package com.example.nashitimer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("nashitimer_settings")

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            focusDurationMin = prefs[Keys.FOCUS_MIN] ?: 25,
            shortBreakMin = prefs[Keys.SHORT_BREAK_MIN] ?: 5,
            longBreakMin = prefs[Keys.LONG_BREAK_MIN] ?: 15,
            longBreakInterval = prefs[Keys.LONG_BREAK_INTERVAL] ?: 4,
            dailyGoal = prefs[Keys.DAILY_GOAL] ?: 8,
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
            debugModeEnabled = prefs[Keys.DEBUG_MODE] ?: false,
            debugFocusDurationSec = prefs[Keys.DEBUG_FOCUS_SEC] ?: 30
        )
    }

    suspend fun update(transform: AppSettings.() -> AppSettings) {
        context.settingsDataStore.edit { prefs ->
            val next = AppSettings(
                focusDurationMin = prefs[Keys.FOCUS_MIN] ?: 25,
                shortBreakMin = prefs[Keys.SHORT_BREAK_MIN] ?: 5,
                longBreakMin = prefs[Keys.LONG_BREAK_MIN] ?: 15,
                longBreakInterval = prefs[Keys.LONG_BREAK_INTERVAL] ?: 4,
                dailyGoal = prefs[Keys.DAILY_GOAL] ?: 8,
                themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.DARK,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
                debugModeEnabled = prefs[Keys.DEBUG_MODE] ?: false,
                debugFocusDurationSec = prefs[Keys.DEBUG_FOCUS_SEC] ?: 30
            ).transform()
            prefs[Keys.FOCUS_MIN] = next.focusDurationMin
            prefs[Keys.SHORT_BREAK_MIN] = next.shortBreakMin
            prefs[Keys.LONG_BREAK_MIN] = next.longBreakMin
            prefs[Keys.LONG_BREAK_INTERVAL] = next.longBreakInterval
            prefs[Keys.DAILY_GOAL] = next.dailyGoal
            prefs[Keys.THEME] = next.themeMode.name
            prefs[Keys.VIBRATION] = next.vibrationEnabled
            prefs[Keys.DEBUG_MODE] = next.debugModeEnabled
            prefs[Keys.DEBUG_FOCUS_SEC] = next.debugFocusDurationSec
        }
    }

    private object Keys {
        val FOCUS_MIN = intPreferencesKey("focus_min")
        val SHORT_BREAK_MIN = intPreferencesKey("short_break_min")
        val LONG_BREAK_MIN = intPreferencesKey("long_break_min")
        val LONG_BREAK_INTERVAL = intPreferencesKey("long_break_interval")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val THEME = stringPreferencesKey("theme")
        val VIBRATION = booleanPreferencesKey("vibration")
        val DEBUG_MODE = booleanPreferencesKey("debug_mode")
        val DEBUG_FOCUS_SEC = intPreferencesKey("debug_focus_sec")
    }
}
