package com.dreaminko.nashipomodoro.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreaminko.nashipomodoro.R
import com.dreaminko.nashipomodoro.ui.components.NashiSwitch
import com.dreaminko.nashipomodoro.ui.components.PageTitle

@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var durationText by remember { mutableStateOf(state.settings.debugFocusDurationSec.toString()) }

    LaunchedEffect(state.settings.debugFocusDurationSec) {
        durationText = state.settings.debugFocusDurationSec.toString()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(
                start = 24.dp,
                top = 18.dp,
                end = 24.dp,
                bottom = 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                )
            }
            Column(Modifier.padding(start = 12.dp)) {
                PageTitle(stringResource(R.string.debug_title))
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DebugCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.debug_mode),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                stringResource(R.string.debug_mode_description),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NashiSwitch(
                            checked = state.settings.debugModeEnabled,
                            onCheckedChange = viewModel::setDebugMode
                        )
                    }

                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { value ->
                            durationText = value.filter(Char::isDigit).take(4)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.settings.debugModeEnabled,
                        label = { Text(stringResource(R.string.debug_focus_duration)) },
                        suffix = { Text(stringResource(R.string.unit_seconds)) },
                        supportingText = { Text(stringResource(R.string.debug_duration_range)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            viewModel.setDebugFocusDurationSeconds(durationText.toIntOrNull() ?: 1)
                        },
                        enabled = state.settings.debugModeEnabled,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.action_apply_test_duration))
                    }
                }
            }

            item {
                DebugCard {
                    Text(
                        stringResource(R.string.debug_glyph_test),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = viewModel::showFullGlyph, modifier = Modifier.weight(1f)) {
                            Text("100%")
                        }
                        Button(onClick = viewModel::showHalfGlyph, modifier = Modifier.weight(1f)) {
                            Text("50%")
                        }
                        Button(onClick = viewModel::turnOffGlyph, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.action_off))
                        }
                    }
                }
            }

            item {
                DebugCard {
                    Text(
                        stringResource(R.string.debug_device),
                        style = MaterialTheme.typography.titleMedium
                    )
                    DebugValue(stringResource(R.string.debug_manufacturer), state.manufacturer)
                    DebugValue(stringResource(R.string.debug_brand), state.brand)
                    DebugValue(stringResource(R.string.debug_model), state.model)
                    DebugValue(stringResource(R.string.debug_profile), state.profile)
                    DebugValue(stringResource(R.string.debug_progress_channel), state.progressChannel)
                    DebugValue(
                        stringResource(R.string.debug_glyph_bar_supported),
                        localizedBoolean(state.supportsGlyphBar)
                    )
                }
            }

            item {
                DebugCard {
                    Text(
                        stringResource(R.string.debug_glyph_session),
                        style = MaterialTheme.typography.titleMedium
                    )
                    DebugValue(
                        stringResource(R.string.debug_service_connected),
                        localizedBoolean(state.glyph.serviceConnected)
                    )
                    DebugValue(
                        stringResource(R.string.debug_registered),
                        localizedBoolean(state.glyph.registered)
                    )
                    DebugValue(
                        stringResource(R.string.debug_session_open),
                        localizedBoolean(state.glyph.sessionOpen)
                    )
                    DebugValue(
                        stringResource(R.string.debug_registration_target),
                        state.glyph.registrationTarget
                            ?: stringResource(R.string.value_not_available)
                    )
                    DebugValue(
                        stringResource(R.string.debug_last_effect),
                        state.glyph.lastEffect ?: stringResource(R.string.value_not_available)
                    )
                    DebugValue(
                        stringResource(R.string.debug_last_error),
                        state.glyph.lastError ?: stringResource(R.string.value_none)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun DebugValue(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            modifier = Modifier.padding(start = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun localizedBoolean(value: Boolean): String =
    stringResource(if (value) R.string.value_yes else R.string.value_no)
