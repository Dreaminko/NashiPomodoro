package com.example.nashitimer.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nashitimer.data.repository.TaskRepository
import com.example.nashitimer.domain.model.TaskItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    val tasks: StateFlow<List<TaskItem>> = repository.tasks.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    fun add(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.add(title.trim()) }
    }

    fun toggle(task: TaskItem) {
        viewModelScope.launch { repository.update(task.copy(isCompleted = !task.isCompleted)) }
    }

    fun delete(task: TaskItem) {
        viewModelScope.launch { repository.delete(task) }
    }
}
