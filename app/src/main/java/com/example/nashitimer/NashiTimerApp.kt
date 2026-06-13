package com.example.nashitimer

import android.app.Application
import com.example.nashitimer.core.timer.TimerRuntime
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NashiTimerApp : Application() {
    @Inject
    lateinit var timerRuntime: TimerRuntime

    override fun onCreate() {
        super.onCreate()
        timerRuntime.initialize()
    }
}
