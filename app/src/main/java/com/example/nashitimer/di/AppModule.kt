package com.example.nashitimer.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nashitimer.data.local.PomodoroDao
import com.example.nashitimer.data.local.PomodoroDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_pomodoro_sessions_createdAt " +
                "ON pomodoro_sessions(createdAt)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_pomodoro_sessions_taskId " +
                "ON pomodoro_sessions(taskId)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_tasks_isCompleted_createdAt " +
                "ON tasks(isCompleted, createdAt)"
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM pomodoro_sessions
            WHERE id NOT IN (
                SELECT MIN(id)
                FROM pomodoro_sessions
                GROUP BY startTime, endTime, phase
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                "index_pomodoro_sessions_startTime_endTime_phase " +
                "ON pomodoro_sessions(startTime, endTime, phase)"
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PomodoroDatabase =
        Room.databaseBuilder(context, PomodoroDatabase::class.java, "nashitimer.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun providePomodoroDao(database: PomodoroDatabase): PomodoroDao = database.pomodoroDao()
}
