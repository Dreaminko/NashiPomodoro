package com.dreaminko.nashipomodoro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem

@Database(
    entities = [PomodoroSession::class, TaskItem::class],
    version = 3,
    exportSchema = true
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroDao(): PomodoroDao
}
