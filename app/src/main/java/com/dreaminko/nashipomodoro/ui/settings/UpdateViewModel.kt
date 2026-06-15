package com.dreaminko.nashipomodoro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreaminko.nashipomodoro.core.update.AppUpdate
import com.dreaminko.nashipomodoro.core.update.UpdateCheckResult
import com.dreaminko.nashipomodoro.core.update.UpdateDownloader
import com.dreaminko.nashipomodoro.core.update.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Available(val update: AppUpdate) : UpdateUiState
    data object DownloadStarted : UpdateUiState
    data object Error : UpdateUiState
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updateDownloader: UpdateDownloader
) : ViewModel() {
    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    fun checkForUpdate() {
        if (_uiState.value == UpdateUiState.Checking) return
        _uiState.value = UpdateUiState.Checking
        viewModelScope.launch {
            _uiState.value = runCatching { updateRepository.checkForUpdate() }
                .fold(
                    onSuccess = { result ->
                        when (result) {
                            is UpdateCheckResult.Available ->
                                UpdateUiState.Available(result.update)
                            UpdateCheckResult.UpToDate -> UpdateUiState.UpToDate
                        }
                    },
                    onFailure = { UpdateUiState.Error }
                )
        }
    }

    fun downloadUpdate(update: AppUpdate) {
        runCatching { updateDownloader.download(update) }
            .onSuccess { _uiState.value = UpdateUiState.DownloadStarted }
            .onFailure { _uiState.value = UpdateUiState.Error }
    }

    fun dismissResult() {
        if (_uiState.value != UpdateUiState.Checking) {
            _uiState.value = UpdateUiState.Idle
        }
    }
}
