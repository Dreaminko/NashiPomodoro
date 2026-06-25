package com.dreaminko.nashipomodoro.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreaminko.nashipomodoro.R
import com.dreaminko.nashipomodoro.data.backup.DataImportSummary

@Composable
fun DataBackupScreen(
    onBack: () -> Unit,
    viewModel: DataBackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showImportConfirmation by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let(viewModel::exportBackup)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(viewModel::importBackup)
    }

    SettingsPage(
        title = stringResource(R.string.settings_data_section),
        onBack = onBack
    ) {
        item {
            SettingsCard {
                SettingText(
                    label = stringResource(R.string.backup_restore_title),
                    description = stringResource(R.string.backup_restore_description)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = !uiState.isBusy,
                        onClick = {
                            if (viewModel.isTimerRunning()) {
                                viewModel.showTimerRunningMessage()
                            } else {
                                exportLauncher.launch("nashipomodoro-backup.json")
                            }
                        }
                    ) {
                        Text(stringResource(R.string.backup_export))
                    }
                    TextButton(
                        enabled = !uiState.isBusy,
                        onClick = {
                            if (viewModel.isTimerRunning()) {
                                viewModel.showTimerRunningMessage()
                            } else {
                                showImportConfirmation = true
                            }
                        }
                    ) {
                        Text(stringResource(R.string.backup_import))
                    }
                    if (uiState.isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text(stringResource(R.string.backup_import_confirm_title)) },
            text = { Text(stringResource(R.string.backup_import_confirm_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                        importLauncher.launch(
                            arrayOf(
                                "application/json",
                                "text/*",
                                "application/octet-stream"
                            )
                        )
                    }
                ) {
                    Text(stringResource(R.string.backup_import))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    DataBackupDialogHost(
        dialog = uiState.dialog,
        onDismiss = viewModel::dismissDialog
    )
}

@Composable
private fun DataBackupDialogHost(
    dialog: DataBackupDialog?,
    onDismiss: () -> Unit
) {
    val title: String
    val message: String
    when (dialog) {
        null -> return
        DataBackupDialog.TimerRunning -> {
            title = stringResource(R.string.backup_timer_running_title)
            message = stringResource(R.string.backup_timer_running_description)
        }
        DataBackupDialog.ExportSuccess -> {
            title = stringResource(R.string.backup_export_success_title)
            message = stringResource(R.string.backup_export_success_description)
        }
        is DataBackupDialog.ImportSuccess -> {
            title = stringResource(R.string.backup_import_success_title)
            message = dialog.summary.importSummaryMessage()
        }
        is DataBackupDialog.Error -> {
            title = stringResource(R.string.backup_error_title)
            message = dialog.message
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(title) },
        text = { Text(message) }
    )
}

@Composable
private fun DataImportSummary.importSummaryMessage(): String =
    stringResource(
        R.string.backup_import_success_description,
        tasksAdded,
        tasksUpdated,
        sessionsAdded,
        sessionsSkipped
    )
