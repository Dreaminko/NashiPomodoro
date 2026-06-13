package com.example.nashitimer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TaskItem

@Database(
    entities = [PomodoroSession::class, TaskItem::class],
    version = 3,
    exportSchema = true
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroDao(): PomodoroDao
}
