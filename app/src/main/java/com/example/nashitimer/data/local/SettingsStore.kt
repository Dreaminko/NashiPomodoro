package com.example.nashitimer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.GlyphChannel
import com.example.nashitimer.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("nashitimer_settings")

@Singleton
class SettingsStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            AppSettings(
                focusDurationMin = prefs[Keys.FOCUS_MIN] ?: 25,
                shortBreakMin = prefs[Keys.SHORT_BREAK_MIN] ?: 5,
                longBreakMin = prefs[Keys.LONG_BREAK_MIN] ?: 15,
                longBreakInterval = prefs[Keys.LONG_BREAK_INTERVAL] ?: 4,
                dailyGoal = prefs[Keys.DAILY_GOAL] ?: 8,
                themeMode = prefs[Keys.THEME]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.DARK,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
                vibrationIntensity = (prefs[Keys.VIBRATION_INTENSITY] ?: 60)
                    .coerceIn(10, 100),
                glyphProgressEnabled = prefs[Keys.GLYPH_PROGRESS] ?: true,
                glyphProgressChannel = prefs[Keys.GLYPH_PROGRESS_CHANNEL].toGlyphChannel(),
                glyphShortBreakProgressEnabled =
                    prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS]
                        ?: prefs[Keys.GLYPH_BREAK_ANIMATION]
                        ?: true,
                glyphShortBreakProgressChannel =
                    prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS_CHANNEL].toGlyphChannel(),
                glyphLongBreakProgressEnabled =
                    prefs[Keys.GLYPH_LONG_BREAK_PROGRESS]
                        ?: prefs[Keys.GLYPH_BREAK_ANIMATION]
                        ?: true,
                glyphLongBreakProgressChannel =
                    prefs[Keys.GLYPH_LONG_BREAK_PROGRESS_CHANNEL].toGlyphChannel(),
                glyphCompletionFlashEnabled = prefs[Keys.GLYPH_COMPLETION_FLASH] ?: true,
                debugModeEnabled = prefs[Keys.DEBUG_MODE] ?: false,
                debugFocusDurationSec = prefs[Keys.DEBUG_FOCUS_SEC] ?: 30
            ).normalized()
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
                vibrationIntensity = (prefs[Keys.VIBRATION_INTENSITY] ?: 60).coerceIn(10, 100),
                glyphProgressEnabled = prefs[Keys.GLYPH_PROGRESS] ?: true,
                glyphProgressChannel = prefs[Keys.GLYPH_PROGRESS_CHANNEL].toGlyphChannel(),
                glyphShortBreakProgressEnabled =
                    prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS]
                        ?: prefs[Keys.GLYPH_BREAK_ANIMATION]
                        ?: true,
                glyphShortBreakProgressChannel =
                    prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS_CHANNEL].toGlyphChannel(),
                glyphLongBreakProgressEnabled =
                    prefs[Keys.GLYPH_LONG_BREAK_PROGRESS]
                        ?: prefs[Keys.GLYPH_BREAK_ANIMATION]
                        ?: true,
                glyphLongBreakProgressChannel =
                    prefs[Keys.GLYPH_LONG_BREAK_PROGRESS_CHANNEL].toGlyphChannel(),
                glyphCompletionFlashEnabled = prefs[Keys.GLYPH_COMPLETION_FLASH] ?: true,
                debugModeEnabled = prefs[Keys.DEBUG_MODE] ?: false,
                debugFocusDurationSec = prefs[Keys.DEBUG_FOCUS_SEC] ?: 30
            ).transform().normalized()
            prefs[Keys.FOCUS_MIN] = next.focusDurationMin
            prefs[Keys.SHORT_BREAK_MIN] = next.shortBreakMin
            prefs[Keys.LONG_BREAK_MIN] = next.longBreakMin
            prefs[Keys.LONG_BREAK_INTERVAL] = next.longBreakInterval
            prefs[Keys.DAILY_GOAL] = next.dailyGoal
            prefs[Keys.THEME] = next.themeMode.name
            prefs[Keys.VIBRATION] = next.vibrationEnabled
            prefs[Keys.VIBRATION_INTENSITY] = next.vibrationIntensity.coerceIn(10, 100)
            prefs[Keys.GLYPH_PROGRESS] = next.glyphProgressEnabled
            prefs[Keys.GLYPH_PROGRESS_CHANNEL] = next.glyphProgressChannel.name
            prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS] = next.glyphShortBreakProgressEnabled
            prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS_CHANNEL] =
                next.glyphShortBreakProgressChannel.name
            prefs[Keys.GLYPH_LONG_BREAK_PROGRESS] = next.glyphLongBreakProgressEnabled
            prefs[Keys.GLYPH_LONG_BREAK_PROGRESS_CHANNEL] =
                next.glyphLongBreakProgressChannel.name
            prefs[Keys.GLYPH_COMPLETION_FLASH] = next.glyphCompletionFlashEnabled
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
        val VIBRATION_INTENSITY = intPreferencesKey("vibration_intensity")
        val GLYPH_PROGRESS = booleanPreferencesKey("glyph_progress")
        val GLYPH_PROGRESS_CHANNEL = stringPreferencesKey("glyph_progress_channel")
        val GLYPH_BREAK_ANIMATION = booleanPreferencesKey("glyph_break_animation")
        val GLYPH_SHORT_BREAK_PROGRESS = booleanPreferencesKey("glyph_short_break_progress")
        val GLYPH_SHORT_BREAK_PROGRESS_CHANNEL =
            stringPreferencesKey("glyph_short_break_progress_channel")
        val GLYPH_LONG_BREAK_PROGRESS = booleanPreferencesKey("glyph_long_break_progress")
        val GLYPH_LONG_BREAK_PROGRESS_CHANNEL =
            stringPreferencesKey("glyph_long_break_progress_channel")
        val GLYPH_COMPLETION_FLASH = booleanPreferencesKey("glyph_completion_flash")
        val DEBUG_MODE = booleanPreferencesKey("debug_mode")
        val DEBUG_FOCUS_SEC = intPreferencesKey("debug_focus_sec")
    }
}

private fun String?.toGlyphChannel(): GlyphChannel =
    this?.let { runCatching { GlyphChannel.valueOf(it) }.getOrNull() } ?: GlyphChannel.AUTO
