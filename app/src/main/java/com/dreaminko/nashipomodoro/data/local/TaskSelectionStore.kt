package com.dreaminko.nashipomodoro.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskSelectionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun load(): Long? = preferences.getLong(KEY_TASK_ID, NO_TASK_ID)
        .takeIf { it > 0L }

    fun save(taskId: Long?) {
        preferences.edit {
            putLong(KEY_TASK_ID, taskId?.takeIf { it > 0L } ?: NO_TASK_ID)
        }
    }

    private companion object {
        const val FILE_NAME = "selected_focus_task"
        const val KEY_TASK_ID = "task_id"
        const val NO_TASK_ID = -1L
    }
}
