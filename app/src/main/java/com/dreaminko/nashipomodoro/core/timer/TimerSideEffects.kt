package com.dreaminko.nashipomodoro.core.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dreaminko.nashipomodoro.core.glyph.GlyphController
import com.dreaminko.nashipomodoro.core.glyph.GlyphEffect
import com.dreaminko.nashipomodoro.core.glyph.GlyphProgressSource
import com.dreaminko.nashipomodoro.core.haptics.VibrationController
import com.dreaminko.nashipomodoro.core.service.PomodoroService
import com.dreaminko.nashipomodoro.data.repository.HistoryRepository
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerForegroundServiceController @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var wasRunning = false

    fun sync(state: TimerState) {
        if (state.isRunning) {
            val serviceIntent = Intent(context, PomodoroService::class.java)
                .setAction(PomodoroService.ACTION_SYNC)
                .putExtra(PomodoroService.EXTRA_TIME, state.timeText)
                .putExtra(PomodoroService.EXTRA_REMAINING_MS, state.remainingMs)
                .putExtra(PomodoroService.EXTRA_TOTAL_MS, state.totalMs)
            if (wasRunning) {
                context.startService(serviceIntent)
            } else {
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        } else if (wasRunning) {
            stop()
        }
        wasRunning = state.isRunning
    }

    fun stop() {
        context.stopService(Intent(context, PomodoroService::class.java))
        wasRunning = false
    }
}

@Singleton
class TimerGlyphEffectController @Inject constructor(
    private val glyphController: GlyphController
) {
    private var progressActive = false

    fun syncProgress(state: TimerState, settings: AppSettings) {
        val effect = progressEffect(state, settings)
        if (effect != GlyphEffect.Off) {
            glyphController.show(effect)
            progressActive = true
        } else if (progressActive) {
            glyphController.show(GlyphEffect.Off)
            progressActive = false
        }
    }

    fun showCompletionFlash() {
        glyphController.show(GlyphEffect.CompleteFlash)
    }

    private fun progressEffect(state: TimerState, settings: AppSettings): GlyphEffect = when {
        state.isRunning &&
            state.phase == TimerPhase.FOCUS &&
            settings.glyphProgressEnabled ->
            GlyphEffect.FocusProgress(
                remainingMs = state.remainingMs,
                totalMs = state.totalMs,
                channel = settings.glyphProgressChannel,
                direction = settings.glyphProgressDirection,
                source = GlyphProgressSource.FOCUS
            )
        state.isRunning &&
            state.phase == TimerPhase.SHORT_BREAK &&
            settings.glyphShortBreakProgressEnabled ->
            GlyphEffect.FocusProgress(
                remainingMs = state.remainingMs,
                totalMs = state.totalMs,
                channel = settings.glyphShortBreakProgressChannel,
                direction = settings.glyphShortBreakProgressDirection,
                source = GlyphProgressSource.SHORT_BREAK
            )
        state.isRunning &&
            state.phase == TimerPhase.LONG_BREAK &&
            settings.glyphLongBreakProgressEnabled ->
            GlyphEffect.FocusProgress(
                remainingMs = state.remainingMs,
                totalMs = state.totalMs,
                channel = settings.glyphLongBreakProgressChannel,
                direction = settings.glyphLongBreakProgressDirection,
                source = GlyphProgressSource.LONG_BREAK
            )
        else -> GlyphEffect.Off
    }
}

@Singleton
class TimerCompletionHandler @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val glyphEffects: TimerGlyphEffectController,
    private val vibrationController: VibrationController
) {
    suspend fun handle(completion: FocusCompletion, settings: AppSettings) {
        val inserted = historyRepository.add(
            PomodoroSession(
                startTime = completion.startTime,
                endTime = completion.endTime,
                phase = TimerPhase.FOCUS.name,
                durationMs = completion.durationMs,
                completed = true,
                taskId = completion.taskId,
                tag = FOCUS_TAG,
                createdAt = completion.endTime
            )
        )
        if (!inserted) return
        if (settings.glyphCompletionFlashEnabled) {
            glyphEffects.showCompletionFlash()
        }
        if (settings.vibrationEnabled) {
            vibrationController.notifyTimerCompletion(settings.vibrationAmplitude)
        }
    }

    private companion object {
        const val FOCUS_TAG = "Focus"
    }
}
