package com.example.nashitimer.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nashitimer.data.repository.HistoryRepository
import com.example.nashitimer.domain.model.PomodoroSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: HistoryRepository
) : ViewModel() {
    val sessions: StateFlow<List<PomodoroSession>> = repository.allSessions().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
}
