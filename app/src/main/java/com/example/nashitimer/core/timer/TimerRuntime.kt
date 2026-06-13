package com.example.nashitimer.core.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.nashitimer.core.glyph.GlyphController
import com.example.nashitimer.core.glyph.GlyphEffect
import com.example.nashitimer.core.haptics.VibrationController
import com.example.nashitimer.core.service.PomodoroService
import com.example.nashitimer.data.local.TaskSelectionStore
import com.example.nashitimer.data.local.TimerSessionStore
import com.example.nashitimer.data.repository.HistoryRepository
import com.example.nashitimer.data.repository.SettingsRepository
import com.example.nashitimer.data.repository.TaskRepository
import com.example.nashitimer.domain.model.AppSettings
import com.example.nashitimer.domain.model.PomodoroSession
import com.example.nashitimer.domain.model.TaskItem
import com.example.nashitimer.domain.model.TimerPhase
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
                glyphController.show(GlyphEffect.CompleteFlash)
                val currentSettings = settings.value
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

                when {
                    state.isRunning && state.phase == TimerPhase.FOCUS ->
                        glyphController.show(
                            GlyphEffect.FocusProgress(
                                remainingMs = state.remainingMs,
                                totalMs = state.totalMs
                            )
                        )
                    state.isRunning && state.phase == TimerPhase.SHORT_BREAK ->
                        glyphController.show(GlyphEffect.ShortBreak)
                    state.isRunning && state.phase == TimerPhase.LONG_BREAK ->
                        glyphController.show(GlyphEffect.LongBreak)
                    wasRunning -> glyphController.show(GlyphEffect.Off)
                }
                wasRunning = state.isRunning
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
