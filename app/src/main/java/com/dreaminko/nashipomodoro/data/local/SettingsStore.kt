package com.dreaminko.nashipomodoro.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection
import com.dreaminko.nashipomodoro.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("nashipomodoro_settings")

@Singleton
class SettingsStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.toAppSettings() }

    suspend fun update(transform: AppSettings.() -> AppSettings) {
        context.settingsDataStore.edit { prefs ->
            val next = prefs.toAppSettings().transform().normalized()
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
            prefs[Keys.GLYPH_PROGRESS_DIRECTION] = next.glyphProgressDirection.name
            prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS] = next.glyphShortBreakProgressEnabled
            prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS_CHANNEL] =
                next.glyphShortBreakProgressChannel.name
            prefs[Keys.GLYPH_SHORT_BREAK_PROGRESS_DIRECTION] =
                next.glyphShortBreakProgressDirection.name
            prefs[Keys.GLYPH_LONG_BREAK_PROGRESS] = next.glyphLongBreakProgressEnabled
            prefs[Keys.GLYPH_LONG_BREAK_PROGRESS_CHANNEL] =
                next.glyphLongBreakProgressChannel.name
            prefs[Keys.GLYPH_LONG_BREAK_PROGRESS_DIRECTION] =
                next.glyphLongBreakProgressDirection.name
            prefs[Keys.GLYPH_COMPLETION_FLASH] = next.glyphCompletionFlashEnabled
            prefs[Keys.DEBUG_MODE] = next.debugModeEnabled
            prefs[Keys.DEBUG_FOCUS_SEC] = next.debugFocusDurationSec
        }
    }

    private fun Preferences.toAppSettings(): AppSettings = AppSettings(
        focusDurationMin = this[Keys.FOCUS_MIN] ?: 25,
        shortBreakMin = this[Keys.SHORT_BREAK_MIN] ?: 5,
        longBreakMin = this[Keys.LONG_BREAK_MIN] ?: 15,
        longBreakInterval = this[Keys.LONG_BREAK_INTERVAL] ?: 4,
        dailyGoal = this[Keys.DAILY_GOAL] ?: 8,
        themeMode = this[Keys.THEME]
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.DARK,
        vibrationEnabled = this[Keys.VIBRATION] ?: true,
        vibrationIntensity = (this[Keys.VIBRATION_INTENSITY] ?: 60).coerceIn(10, 100),
        glyphProgressEnabled = this[Keys.GLYPH_PROGRESS] ?: true,
        glyphProgressChannel = this[Keys.GLYPH_PROGRESS_CHANNEL].toGlyphChannel(),
        glyphProgressDirection =
            this[Keys.GLYPH_PROGRESS_DIRECTION].toGlyphProgressDirection(),
        glyphShortBreakProgressEnabled =
            this[Keys.GLYPH_SHORT_BREAK_PROGRESS]
                ?: this[Keys.GLYPH_BREAK_ANIMATION]
                ?: true,
        glyphShortBreakProgressChannel =
            this[Keys.GLYPH_SHORT_BREAK_PROGRESS_CHANNEL].toGlyphChannel(),
        glyphShortBreakProgressDirection =
            this[Keys.GLYPH_SHORT_BREAK_PROGRESS_DIRECTION].toGlyphProgressDirection(),
        glyphLongBreakProgressEnabled =
            this[Keys.GLYPH_LONG_BREAK_PROGRESS]
                ?: this[Keys.GLYPH_BREAK_ANIMATION]
                ?: true,
        glyphLongBreakProgressChannel =
            this[Keys.GLYPH_LONG_BREAK_PROGRESS_CHANNEL].toGlyphChannel(),
        glyphLongBreakProgressDirection =
            this[Keys.GLYPH_LONG_BREAK_PROGRESS_DIRECTION].toGlyphProgressDirection(),
        glyphCompletionFlashEnabled = this[Keys.GLYPH_COMPLETION_FLASH] ?: true,
        debugModeEnabled = this[Keys.DEBUG_MODE] ?: false,
        debugFocusDurationSec = this[Keys.DEBUG_FOCUS_SEC] ?: 30
    ).normalized()

    suspend fun currentSettings(): AppSettings = settings.first()

    suspend fun importUserSettings(imported: AppSettings) {
        update {
            imported.normalized().copy(
                debugModeEnabled = debugModeEnabled,
                debugFocusDurationSec = debugFocusDurationSec
            )
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
        val GLYPH_PROGRESS_DIRECTION = stringPreferencesKey("glyph_progress_direction")
        val GLYPH_BREAK_ANIMATION = booleanPreferencesKey("glyph_break_animation")
        val GLYPH_SHORT_BREAK_PROGRESS = booleanPreferencesKey("glyph_short_break_progress")
        val GLYPH_SHORT_BREAK_PROGRESS_CHANNEL =
            stringPreferencesKey("glyph_short_break_progress_channel")
        val GLYPH_SHORT_BREAK_PROGRESS_DIRECTION =
            stringPreferencesKey("glyph_short_break_progress_direction")
        val GLYPH_LONG_BREAK_PROGRESS = booleanPreferencesKey("glyph_long_break_progress")
        val GLYPH_LONG_BREAK_PROGRESS_CHANNEL =
            stringPreferencesKey("glyph_long_break_progress_channel")
        val GLYPH_LONG_BREAK_PROGRESS_DIRECTION =
            stringPreferencesKey("glyph_long_break_progress_direction")
        val GLYPH_COMPLETION_FLASH = booleanPreferencesKey("glyph_completion_flash")
        val DEBUG_MODE = booleanPreferencesKey("debug_mode")
        val DEBUG_FOCUS_SEC = intPreferencesKey("debug_focus_sec")
    }
}

private fun String?.toGlyphChannel(): GlyphChannel =
    this?.let { runCatching { GlyphChannel.valueOf(it) }.getOrNull() } ?: GlyphChannel.AUTO

private fun String?.toGlyphProgressDirection(): GlyphProgressDirection =
    this?.let { runCatching { GlyphProgressDirection.valueOf(it) }.getOrNull() }
        ?: GlyphProgressDirection.FORWARD
