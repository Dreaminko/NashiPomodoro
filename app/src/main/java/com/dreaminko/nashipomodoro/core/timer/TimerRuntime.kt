package com.dreaminko.nashipomodoro.core.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dreaminko.nashipomodoro.core.glyph.GlyphController
import com.dreaminko.nashipomodoro.core.glyph.GlyphEffect
import com.dreaminko.nashipomodoro.core.glyph.GlyphProgressSource
import com.dreaminko.nashipomodoro.core.haptics.VibrationController
import com.dreaminko.nashipomodoro.core.service.PomodoroService
import com.dreaminko.nashipomodoro.data.local.TaskSelectionStore
import com.dreaminko.nashipomodoro.data.local.TimerSessionStore
import com.dreaminko.nashipomodoro.data.repository.HistoryRepository
import com.dreaminko.nashipomodoro.data.repository.SettingsRepository
import com.dreaminko.nashipomodoro.data.repository.TaskRepository
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.PomodoroSession
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class TimerUiState(
    val timer: TimerState = TimerState(),
    val settings: AppSettings = AppSettings(),
    val activeTask: TaskItem? = null
)

private data class TimerPersistenceKey(
    val phase: TimerPhase,
    val activePhase: TimerPhase,
    val pausedRemainingMs: Long,
    val totalMs: Long,
    val completedFocusRounds: Int,
    val isRunning: Boolean,
    val taskId: Long?
)

@Singleton
class TimerRuntime @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val engine: TimerEngine,
    settingsRepository: SettingsRepository,
    private val timerSessionStore: TimerSessionStore,
    private val taskSelectionStore: TaskSelectionStore,
    taskRepository: TaskRepository,
    private val historyRepository: HistoryRepository,
    private val glyphController: GlyphController,
    private val vibrationController: VibrationController
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings = settingsRepository.settings.stateIn(
        scope,
        SharingStarted.Eagerly,
        AppSettings()
    )
    private val selectedTaskId = MutableStateFlow(taskSelectionStore.load())
    private val taskItems = taskRepository.tasks
    private val tasks = taskItems.stateIn(
        scope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val uiState: StateFlow<TimerUiState> = combine(
        engine.state,
        settings,
        tasks,
        selectedTaskId
    ) { timer, appSettings, taskItems, selectedId ->
        TimerUiState(
            timer = timer,
            settings = appSettings,
            activeTask = taskItems.firstOrNull { it.id == (timer.taskId ?: selectedId) }
        )
    }.stateIn(scope, SharingStarted.Eagerly, TimerUiState())

    init {
        observeTaskSelection()
        observeSettings()
        observeCompletions()
        observeStateEffects()
        restoreAndPersist(settingsRepository)
    }

    fun initialize() = Unit

    fun toggle() {
        if (engine.state.value.isRunning) {
            engine.pause()
        } else {
            engine.resume(settings.value)
        }
    }

    fun end() {
        engine.stop(settings.value)
        context.stopService(Intent(context, PomodoroService::class.java))
    }

    fun skip() = engine.skip(settings.value)

    fun setFaceDown(faceDown: Boolean) = engine.setFaceDown(faceDown)

    fun resume() = engine.resume(settings.value)

    fun pause() = engine.pause()

    fun selectTask(taskId: Long?) {
        taskSelectionStore.save(taskId)
        selectedTaskId.value = taskId
    }

    private fun observeTaskSelection() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            selectedTaskId.collect(engine::selectTask)
        }
        scope.launch {
            combine(taskItems, selectedTaskId) { availableTasks, taskId ->
                availableTasks to taskId
            }.collect { (availableTasks, taskId) ->
                if (taskId != null &&
                    availableTasks.none { it.id == taskId && !it.isCompleted }
                ) {
                    selectTask(null)
                }
            }
        }
    }

    private fun observeSettings() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            settings.collect(engine::updateSettings)
        }
    }

    private fun observeCompletions() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.focusCompletions.collect { completion ->
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
                if (!inserted) return@collect
                val currentSettings = settings.value
                if (currentSettings.glyphCompletionFlashEnabled) {
                    glyphController.show(GlyphEffect.CompleteFlash)
                }
                if (currentSettings.vibrationEnabled) {
                    vibrationController.notifyTimerCompletion(
                        currentSettings.vibrationAmplitude
                    )
                }
            }
        }
    }

    private fun observeStateEffects() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            var wasRunning = false
            engine.state.collect { state ->
                if (state.isRunning) {
                    val serviceIntent = Intent(context, PomodoroService::class.java)
                        .putExtra(PomodoroService.EXTRA_TIME, state.timeText)
                        .putExtra(PomodoroService.EXTRA_REMAINING_MS, state.remainingMs)
                        .putExtra(PomodoroService.EXTRA_TOTAL_MS, state.totalMs)
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else if (wasRunning) {
                    context.stopService(Intent(context, PomodoroService::class.java))
                }

                wasRunning = state.isRunning
            }
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            var glyphStateActive = false
            combine(engine.state, settings) { state, appSettings ->
                when {
                    state.isRunning &&
                        state.phase == TimerPhase.FOCUS &&
                        appSettings.glyphProgressEnabled ->
                        GlyphEffect.FocusProgress(
                            remainingMs = state.remainingMs,
                            totalMs = state.totalMs,
                            channel = appSettings.glyphProgressChannel,
                            source = GlyphProgressSource.FOCUS
                        )
                    state.isRunning &&
                        state.phase == TimerPhase.SHORT_BREAK &&
                        appSettings.glyphShortBreakProgressEnabled ->
                        GlyphEffect.FocusProgress(
                            remainingMs = state.remainingMs,
                            totalMs = state.totalMs,
                            channel = appSettings.glyphShortBreakProgressChannel,
                            source = GlyphProgressSource.SHORT_BREAK
                        )
                    state.isRunning &&
                        state.phase == TimerPhase.LONG_BREAK &&
                        appSettings.glyphLongBreakProgressEnabled ->
                        GlyphEffect.FocusProgress(
                            remainingMs = state.remainingMs,
                            totalMs = state.totalMs,
                            channel = appSettings.glyphLongBreakProgressChannel,
                            source = GlyphProgressSource.LONG_BREAK
                        )
                    else -> GlyphEffect.Off
                }
            }.collect { effect ->
                if (effect != GlyphEffect.Off) {
                    glyphController.show(effect)
                    glyphStateActive = true
                } else if (glyphStateActive) {
                    glyphController.show(GlyphEffect.Off)
                    glyphStateActive = false
                }
            }
        }
    }

    private fun restoreAndPersist(settingsRepository: SettingsRepository) {
        scope.launch {
            val initialSettings = settingsRepository.settings.first()
            val storedSnapshot = timerSessionStore.load()
            storedSnapshot?.let { snapshot ->
                engine.restore(snapshot, initialSettings)
            }
            if (storedSnapshot == null) timerSessionStore.clear()
            var lastKey: TimerPersistenceKey? = null
            engine.state.collect { state ->
                if (state.phase == TimerPhase.IDLE) {
                    if (lastKey != null) timerSessionStore.clear()
                    lastKey = null
                } else {
                    val snapshot = engine.snapshot()
                    val key = TimerPersistenceKey(
                        phase = snapshot.phase,
                        activePhase = snapshot.activePhase,
                        pausedRemainingMs = if (snapshot.isRunning) 0L else snapshot.remainingMs,
                        totalMs = snapshot.totalMs,
                        completedFocusRounds = snapshot.completedFocusRounds,
                        isRunning = snapshot.isRunning,
                        taskId = snapshot.taskId
                    )
                    if (key != lastKey) {
                        timerSessionStore.save(snapshot)
                        lastKey = key
                    }
                }
            }
        }
    }

    private companion object {
        const val FOCUS_TAG = "Focus"
    }
}
