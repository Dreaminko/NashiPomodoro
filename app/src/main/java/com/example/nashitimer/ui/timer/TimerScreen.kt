package com.example.nashitimer.ui.timer

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nashitimer.domain.model.TimerPhase
import com.example.nashitimer.ui.components.NothingButton
import com.example.nashitimer.ui.components.ProgressRing

@Composable
fun TimerScreen(
    onOpenSettings: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by animateFloatAsState(uiState.timer.progress, label = "timer-progress")
    val activity = LocalContext.current as? Activity

    DisposableEffect(uiState.timer.isFaceDown) {
        val window = activity?.window
        val previous = window?.attributes?.screenBrightness
        if (uiState.timer.isFaceDown && window != null) {
            window.attributes = window.attributes.apply { screenBrightness = 0.01f }
        }
        onDispose {
            if (window != null && previous != null) {
                window.attributes = window.attributes.apply { screenBrightness = previous }
            }
        }
    }

    val accent = phaseColor(uiState.timer.phase)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "NASHI",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Make time feel lighter", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SettingsGlyph()
            }
        }

        Spacer(Modifier.weight(0.6f))

        Box(contentAlignment = Alignment.Center) {
            ProgressRing(progress = progress, activeColor = accent)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    contentColor = accent,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = uiState.timer.phase.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = uiState.timer.timeText,
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light
                )
                Text(
                    if (uiState.timer.isRunning) "Stay with this moment" else "Ready when you are",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.weight(0.6f))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(
                            if (uiState.timer.isFaceDown) accent else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        if (uiState.timer.isFaceDown) "Glyph focus is active" else "Flip your phone to begin",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (uiState.timer.isFaceDown) "Screen dimmed to keep distractions away"
                        else "The timer follows the position of your phone",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NothingButton(
                text = "End session",
                modifier = Modifier.weight(1f),
                onClick = viewModel::end
            )
            NothingButton(
                text = if (uiState.timer.isRunning) "Pause" else "Start focus",
                modifier = Modifier.weight(1.25f),
                primary = true,
                onClick = viewModel::toggleManual
            )
        }

        Text(
            text = "Round ${uiState.timer.completedFocusRounds + 1}  |  Long break every ${uiState.settings.longBreakInterval}",
            modifier = Modifier.padding(top = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun phaseColor(phase: TimerPhase): Color = when (phase) {
    TimerPhase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
    TimerPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun SettingsGlyph() {
    Text(
        text = "...",
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
}
