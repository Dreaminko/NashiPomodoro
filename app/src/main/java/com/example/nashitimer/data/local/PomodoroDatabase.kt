package com.example.nashitimer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TaskItem

@Database(
    entities = [PomodoroSession::class, TaskItem::class],
    version = 1,
    exportSchema = false
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroDao(): PomodoroDao
}
