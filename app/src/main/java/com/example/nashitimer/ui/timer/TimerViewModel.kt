package com.example.nashitimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nashitimer.core.sensor.FlipDetector
import com.example.nashitimer.core.timer.TimerRuntime
import com.example.nashitimer.core.timer.TimerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val runtime: TimerRuntime,
    private val flipDetector: FlipDetector
) : ViewModel() {
    val uiState: StateFlow<TimerUiState> = runtime.uiState

    init {
        observeFlips()
    }

    fun toggleManual() = runtime.toggle()

    fun end() = runtime.end()

    fun skip() = runtime.skip()

    private fun observeFlips() {
        viewModelScope.launch {
            flipDetector.faceDownEvents().distinctUntilChanged().collect { faceDown ->
                runtime.setFaceDown(faceDown)
                when {
                    faceDown -> runtime.resume()
                    runtime.uiState.value.timer.isRunning -> runtime.pause()
                }
            }
        }
    }
}
