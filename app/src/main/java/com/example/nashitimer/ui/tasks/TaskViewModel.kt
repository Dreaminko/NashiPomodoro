package com.example.nashitimer.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nashitimer.data.repository.TaskRepository
import com.example.nashitimer.domain.model.TaskItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter {
    ALL,
    OPEN,
    COMPLETED
}

internal fun List<TaskItem>.filteredBy(filter: TaskFilter): List<TaskItem> = when (filter) {
    TaskFilter.ALL -> this
    TaskFilter.OPEN -> filterNot(TaskItem::isCompleted)
    TaskFilter.COMPLETED -> filter(TaskItem::isCompleted)
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    val tasks: StateFlow<List<TaskItem>> = repository.tasks.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter.asStateFlow()

    fun add(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.add(title.trim()) }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun toggle(task: TaskItem) {
        viewModelScope.launch { repository.update(task.copy(isCompleted = !task.isCompleted)) }
    }

    fun update(task: TaskItem, title: String, description: String, pomodoroGoal: Int) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return
        viewModelScope.launch {
            repository.update(
                task.copy(
                    title = normalizedTitle,
                    description = description.trim().ifEmpty { null },
                    pomodoroGoal = pomodoroGoal.coerceIn(1, 99)
                )
            )
        }
    }

    fun delete(task: TaskItem) {
        viewModelScope.launch { repository.delete(task) }
    }

    fun clearCompleted() {
        viewModelScope.launch { repository.clearCompleted() }
    }
}
