package com.dreaminko.nashipomodoro.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dreaminko.nashipomodoro.MainActivity
import com.dreaminko.nashipomodoro.R
import com.dreaminko.nashipomodoro.core.timer.TimerRuntime
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroService : Service() {
    @Inject
    lateinit var timerRuntime: TimerRuntime
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            timerRuntime.end()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        val updatedNotification = notification(
            time = intent?.getStringExtra(EXTRA_TIME) ?: "--:--",
            remainingMs = intent?.getLongExtra(EXTRA_REMAINING_MS, 0L) ?: 0L,
            totalMs = intent?.getLongExtra(EXTRA_TOTAL_MS, 0L) ?: 0L
        )
        if (isForeground) {
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, updatedNotification)
        } else {
            startForeground(NOTIFICATION_ID, updatedNotification)
            isForeground = true
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isForeground = false
        super.onDestroy()
    }

    private fun notification(
        time: String,
        remainingMs: Long,
        totalMs: Long
    ): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, PomodoroService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val progress = calculateProgress(remainingMs, totalMs)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(time)
            .setContentText(getString(R.string.notification_running))
            .setContentIntent(openIntent)
            .addAction(0, getString(R.string.action_end), stopIntent)
            .setProgress(PROGRESS_MAX, progress, totalMs <= 0L)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return if (Build.VERSION.SDK_INT >= 36 && totalMs > 0L) {
            Notification.Builder.recoverBuilder(this, notification)
                .setStyle(
                    Notification.ProgressStyle()
                        .addProgressSegment(Notification.ProgressStyle.Segment(PROGRESS_MAX))
                        .setProgress(progress)
                        .setStyledByProgress(true)
                )
                .build()
        } else {
            notification
        }
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "nashipomodoro_pomodoro"
        private const val NOTIFICATION_ID = 42
        private const val PROGRESS_MAX = 1000
        const val ACTION_STOP = "com.dreaminko.nashipomodoro.STOP"
        const val ACTION_SYNC = "com.dreaminko.nashipomodoro.SYNC"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_REMAINING_MS = "extra_remaining_ms"
        const val EXTRA_TOTAL_MS = "extra_total_ms"

        private fun calculateProgress(remainingMs: Long, totalMs: Long): Int {
            if (totalMs <= 0L) return 0
            val elapsedFraction = 1.0 - remainingMs.coerceIn(0L, totalMs).toDouble() / totalMs
            return (elapsedFraction * PROGRESS_MAX).toInt().coerceIn(0, PROGRESS_MAX)
        }
    }
}
