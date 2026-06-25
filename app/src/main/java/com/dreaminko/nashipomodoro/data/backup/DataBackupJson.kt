package com.dreaminko.nashipomodoro.data.backup

import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import com.dreaminko.nashipomodoro.domain.model.ThemeMode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object DataBackupJson {
    const val SCHEMA_VERSION = 1

    fun encode(payload: DataBackupPayload): String =
        JSONObject()
            .put("schemaVersion", payload.schemaVersion)
            .put("exportedAt", payload.exportedAt)
            .put("appVersion", payload.appVersion)
            .put("settings", payload.settings.toJson())
            .put("tasks", JSONArray().apply { payload.tasks.forEach { put(it.toJson()) } })
            .put(
                "sessions",
                JSONArray().apply { payload.sessions.forEach { put(it.toJson()) } }
            )
            .toString(2)

    fun decode(raw: String): DataBackupPayload {
        try {
            val root = JSONObject(raw)
            val schemaVersion = root.getInt("schemaVersion")
            if (schemaVersion > SCHEMA_VERSION) {
                throw DataBackupException("Backup file was created by a newer version.")
            }
            if (schemaVersion < 1) {
                throw DataBackupException("Backup file version is not supported.")
            }

            return DataBackupPayload(
                schemaVersion = schemaVersion,
                exportedAt = root.getLong("exportedAt"),
                appVersion = root.optString("appVersion", ""),
                settings = root.getJSONObject("settings").toSettings(),
                tasks = root.getJSONArray("tasks").mapObjects { it.toTask() },
                sessions = root.getJSONArray("sessions").mapObjects { it.toSession() }
            )
        } catch (error: DataBackupException) {
            throw error
        } catch (error: JSONException) {
            throw DataBackupException("Backup file is invalid or corrupted.", error)
        }
    }
}

private fun AppSettings.toJson(): JSONObject = JSONObject()
    .put("focusDurationMin", focusDurationMin)
    .put("shortBreakMin", shortBreakMin)
    .put("longBreakMin", longBreakMin)
    .put("longBreakInterval", longBreakInterval)
    .put("dailyGoal", dailyGoal)
    .put("themeMode", themeMode.name)
    .put("vibrationEnabled", vibrationEnabled)
    .put("vibrationIntensity", vibrationIntensity)
    .put("glyphProgressEnabled", glyphProgressEnabled)
    .put("glyphProgressChannel", glyphProgressChannel.name)
    .put("glyphProgressDirection", glyphProgressDirection.name)
    .put("glyphShortBreakProgressEnabled", glyphShortBreakProgressEnabled)
    .put("glyphShortBreakProgressChannel", glyphShortBreakProgressChannel.name)
    .put("glyphShortBreakProgressDirection", glyphShortBreakProgressDirection.name)
    .put("glyphLongBreakProgressEnabled", glyphLongBreakProgressEnabled)
    .put("glyphLongBreakProgressChannel", glyphLongBreakProgressChannel.name)
    .put("glyphLongBreakProgressDirection", glyphLongBreakProgressDirection.name)
    .put("glyphCompletionFlashEnabled", glyphCompletionFlashEnabled)

private fun JSONObject.toSettings(): AppSettings = AppSettings(
    focusDurationMin = optInt("focusDurationMin", 25),
    shortBreakMin = optInt("shortBreakMin", 5),
    longBreakMin = optInt("longBreakMin", 15),
    longBreakInterval = optInt("longBreakInterval", 4),
    dailyGoal = optInt("dailyGoal", 8),
    themeMode = enumValue("themeMode", ThemeMode.DARK),
    vibrationEnabled = optBoolean("vibrationEnabled", true),
    vibrationIntensity = optInt("vibrationIntensity", 60),
    glyphProgressEnabled = optBoolean("glyphProgressEnabled", true),
    glyphProgressChannel = enumValue("glyphProgressChannel", GlyphChannel.AUTO),
    glyphProgressDirection =
        enumValue("glyphProgressDirection", GlyphProgressDirection.FORWARD),
    glyphShortBreakProgressEnabled = optBoolean("glyphShortBreakProgressEnabled", true),
    glyphShortBreakProgressChannel =
        enumValue("glyphShortBreakProgressChannel", GlyphChannel.AUTO),
    glyphShortBreakProgressDirection =
        enumValue("glyphShortBreakProgressDirection", GlyphProgressDirection.FORWARD),
    glyphLongBreakProgressEnabled = optBoolean("glyphLongBreakProgressEnabled", true),
    glyphLongBreakProgressChannel =
        enumValue("glyphLongBreakProgressChannel", GlyphChannel.AUTO),
    glyphLongBreakProgressDirection =
        enumValue("glyphLongBreakProgressDirection", GlyphProgressDirection.FORWARD),
    glyphCompletionFlashEnabled = optBoolean("glyphCompletionFlashEnabled", true)
).normalized()

private fun TaskItem.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("description", description ?: JSONObject.NULL)
    .put("isCompleted", isCompleted)
    .put("pomodoroGoal", pomodoroGoal)
    .put("pomodoroDone", pomodoroDone)
    .put("createdAt", createdAt)

private fun JSONObject.toTask(): TaskItem = TaskItem(
    id = getLong("id"),
    title = getString("title"),
    description = nullableString("description"),
    isCompleted = optBoolean("isCompleted", false),
    pomodoroGoal = optInt("pomodoroGoal", 1).coerceAtLeast(1),
    pomodoroDone = optInt("pomodoroDone", 0).coerceAtLeast(0),
    createdAt = getLong("createdAt")
)

private fun PomodoroSession.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("startTime", startTime)
    .put("endTime", endTime)
    .put("phase", phase)
    .put("durationMs", durationMs)
    .put("completed", completed)
    .put("taskId", taskId ?: JSONObject.NULL)
    .put("tag", tag ?: JSONObject.NULL)
    .put("createdAt", createdAt)

private fun JSONObject.toSession(): PomodoroSession = PomodoroSession(
    id = getLong("id"),
    startTime = getLong("startTime"),
    endTime = getLong("endTime"),
    phase = getString("phase"),
    durationMs = getLong("durationMs").coerceAtLeast(0L),
    completed = optBoolean("completed", false),
    taskId = nullableLong("taskId"),
    tag = nullableString("tag"),
    createdAt = getLong("createdAt")
)

private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

private inline fun <reified T : Enum<T>> JSONObject.enumValue(
    key: String,
    defaultValue: T
): T = optString(key)
    .takeIf { it.isNotBlank() }
    ?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } }
    ?: defaultValue

private fun JSONObject.nullableString(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null

private fun JSONObject.nullableLong(key: String): Long? =
    if (has(key) && !isNull(key)) getLong(key) else null
