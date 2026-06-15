package com.dreaminko.nashipomodoro

import android.app.Application
import com.dreaminko.nashipomodoro.core.timer.TimerRuntime
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NashiPomodoroApp : Application() {
    @Inject
    lateinit var timerRuntime: TimerRuntime

    override fun onCreate() {
        super.onCreate()
        timerRuntime.initialize()
    }
}
