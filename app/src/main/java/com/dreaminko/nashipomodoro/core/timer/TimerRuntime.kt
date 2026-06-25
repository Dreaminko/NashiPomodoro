package com.dreaminko.nashipomodoro.core.timer

import com.dreaminko.nashipomodoro.data.local.TaskSelectionStore
import com.dreaminko.nashipomodoro.data.local.TimerSessionStore
import com.dreaminko.nashipomodoro.data.repository.SettingsRepository
import com.dreaminko.nashipomodoro.data.repository.TaskRepository
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import com.dreaminko.nashipomodoro.domain.model.TimerPhase
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
    private val engine: TimerEngine,
    settingsRepository: SettingsRepository,
    private val timerSessionStore: TimerSessionStore,
    private val taskSelectionStore: TaskSelectionStore,
    taskRepository: TaskRepository,
    private val foregroundService: TimerForegroundServiceController,
    private val glyphEffects: TimerGlyphEffectController,
    private val completionHandler: TimerCompletionHandler
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


    fun toggle() {
        if (engine.state.value.isRunning) {
            engine.pause()
        } else {
            engine.resume(settings.value)
        }
    }

    fun end() {
        engine.stop(settings.value)
        foregroundService.stop()
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
                completionHandler.handle(completion, settings.value)
            }
        }
    }

    private fun observeStateEffects() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.state.collect(foregroundService::sync)
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            combine(engine.state, settings) { state, appSettings ->
                state to appSettings
            }.collect { (state, appSettings) ->
                glyphEffects.syncProgress(state, appSettings)
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
}
