package com.example.nashitimer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nashitimer.ui.components.PageTitle

@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var durationText by remember { mutableStateOf(state.settings.debugFocusDurationSec.toString()) }

    LaunchedEffect(state.settings.debugFocusDurationSec) {
        durationText = state.settings.debugFocusDurationSec.toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Column(Modifier.padding(start = 8.dp)) {
                    PageTitle("Debug")
                }
            }
        }

        item {
            DebugCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Debug mode", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Use a custom focus duration measured in seconds",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
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
                    label = { Text("Focus duration") },
                    suffix = { Text("seconds") },
                    supportingText = { Text("Allowed range: 1-3600 seconds") },
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
                    Text("Apply test duration")
                }
            }
        }

        item {
            DebugCard {
                Text("Glyph test", style = MaterialTheme.typography.titleMedium)
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
                        Text("Off")
                    }
                }
            }
        }

        item {
            DebugCard {
                Text("Device", style = MaterialTheme.typography.titleMedium)
                DebugValue("Manufacturer", state.manufacturer)
                DebugValue("Brand", state.brand)
                DebugValue("Model", state.model)
                DebugValue("Profile", state.profile)
                DebugValue("Progress channel", state.progressChannel)
                DebugValue("Glyph bar supported", state.supportsGlyphBar.toString())
            }
        }

        item {
            DebugCard {
                Text("Glyph session", style = MaterialTheme.typography.titleMedium)
                DebugValue("Service connected", state.glyph.serviceConnected.toString())
                DebugValue("Registered", state.glyph.registered.toString())
                DebugValue("Session open", state.glyph.sessionOpen.toString())
                DebugValue("Registration target", state.glyph.registrationTarget ?: "N/A")
                DebugValue("Last effect", state.glyph.lastEffect ?: "N/A")
                DebugValue("Last error", state.glyph.lastError ?: "None")
            }
        }
    }
}

@Composable
private fun DebugCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
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
