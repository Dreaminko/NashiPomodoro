package com.example.nashitimer.ui.timer

import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nashitimer.core.glyph.GlyphController
import com.example.nashitimer.core.glyph.GlyphEffect
import com.example.nashitimer.core.sensor.FlipDetector
import com.example.nashitimer.core.service.PomodoroService
import com.example.nashitimer.core.timer.TimerEngine
import com.example.nashitimer.core.timer.TimerState
import com.example.nashitimer.data.repository.HistoryRepository
import com.example.nashitimer.data.repository.SettingsRepository
import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TimerPhase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val timer: TimerState = TimerState(),
    val settings: AppSettings = AppSettings()
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val engine: TimerEngine,
    private val flipDetector: FlipDetector,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val glyphController: GlyphController
) : ViewModel() {
    private val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings()
    )

    val uiState: StateFlow<TimerUiState> = combine(engine.state, settings) { timer, appSettings ->
        TimerUiState(timer, appSettings)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TimerUiState())

    private val lastRound = MutableStateFlow(0)

    init {
        glyphController.init()
        observeFlips()
        observeCompletion()
        observeServiceAndGlyph()
    }

    fun toggleManual() {
        val current = engine.state.value
        if (current.isRunning) engine.pause() else engine.resume(viewModelScope, settings.value)
    }

    fun end() {
        engine.stop(settings.value)
        glyphController.show(GlyphEffect.Off)
        context.stopService(Intent(context, PomodoroService::class.java))
    }

    private fun observeFlips() {
        viewModelScope.launch {
            flipDetector.faceDownEvents().distinctUntilChanged().collect { faceDown ->
                engine.setFaceDown(faceDown)
                when {
                    faceDown -> engine.resume(viewModelScope, settings.value)
                    engine.state.value.isRunning -> engine.pause()
                }
            }
        }
    }

    private fun observeCompletion() {
        viewModelScope.launch {
            engine.state.collect { state ->
                if (state.completedFocusRounds > lastRound.value) {
                    lastRound.value = state.completedFocusRounds
                    val now = System.currentTimeMillis()
                    val focusDurationMs = settings.value.focusDurationMs
                    historyRepository.add(
                        PomodoroSession(
                            startTime = now - focusDurationMs,
                            endTime = now,
                            phase = TimerPhase.FOCUS.name,
                            durationMs = focusDurationMs,
                            completed = true,
                            tag = "Focus",
                            createdAt = now
                        )
                    )
                    glyphController.show(GlyphEffect.CompleteFlash)
                    vibrateIfEnabled()
                }
            }
        }
    }

    private fun observeServiceAndGlyph() {
        viewModelScope.launch {
            engine.state.collect { state ->
                if (state.isRunning) {
                    val serviceIntent = Intent(context, PomodoroService::class.java)
                        .putExtra(PomodoroService.EXTRA_TIME, state.timeText)
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
                when {
                    !state.isRunning -> glyphController.show(GlyphEffect.Off)
                    state.phase == TimerPhase.FOCUS ->
                        glyphController.show(GlyphEffect.FocusProgress(state.remainingFraction))
                    state.phase == TimerPhase.SHORT_BREAK -> glyphController.show(GlyphEffect.ShortBreak)
                    state.phase == TimerPhase.LONG_BREAK -> glyphController.show(GlyphEffect.LongBreak)
                }
            }
        }
    }

    private fun vibrateIfEnabled() {
        if (!settings.value.vibrationEnabled) return
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator.vibrate(VibrationEffect.createOneShot(220, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onCleared() {
        glyphController.release()
    }
}
