package com.dreaminko.nashipomodoro.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreaminko.nashipomodoro.core.timer.TimerRuntime
import com.dreaminko.nashipomodoro.data.backup.DataBackupException
import com.dreaminko.nashipomodoro.data.backup.DataBackupRepository
import com.dreaminko.nashipomodoro.data.backup.DataImportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataBackupViewModel @Inject constructor(
    private val repository: DataBackupRepository,
    private val timerRuntime: TimerRuntime
) : ViewModel() {
    private val _uiState = MutableStateFlow(DataBackupUiState())
    val uiState: StateFlow<DataBackupUiState> = _uiState.asStateFlow()

    fun exportBackup(uri: Uri) {
        if (timerRuntime.uiState.value.timer.isRunning) {
            showDialog(DataBackupDialog.TimerRunning)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, dialog = null) }
            runCatching { repository.exportTo(uri) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isBusy = false, dialog = DataBackupDialog.ExportSuccess)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isBusy = false, dialog = DataBackupDialog.Error(error.userMessage()))
                    }
                }
        }
    }

    fun importBackup(uri: Uri) {
        if (timerRuntime.uiState.value.timer.isRunning) {
            showDialog(DataBackupDialog.TimerRunning)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, dialog = null) }
            runCatching { repository.importFrom(uri) }
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            dialog = DataBackupDialog.ImportSuccess(summary)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isBusy = false, dialog = DataBackupDialog.Error(error.userMessage()))
                    }
                }
        }
    }

    fun showTimerRunningMessage() = showDialog(DataBackupDialog.TimerRunning)

    fun isTimerRunning(): Boolean = timerRuntime.uiState.value.timer.isRunning

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    private fun showDialog(dialog: DataBackupDialog) {
        _uiState.update { it.copy(dialog = dialog) }
    }

    private fun Throwable.userMessage(): String =
        if (this is DataBackupException) message.orEmpty() else "Backup operation failed."
}

data class DataBackupUiState(
    val isBusy: Boolean = false,
    val dialog: DataBackupDialog? = null
)

sealed interface DataBackupDialog {
    data object TimerRunning : DataBackupDialog
    data object ExportSuccess : DataBackupDialog
    data class ImportSuccess(val summary: DataImportSummary) : DataBackupDialog
    data class Error(val message: String) : DataBackupDialog
}
