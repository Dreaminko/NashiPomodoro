package com.example.nashitimer.ui.timer

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Box(Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NashiTimer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onOpenSettings) {
                Text("SET")
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(progress)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.timer.timeText, fontSize = 56.sp, fontFamily = FontFamily.Monospace)
                    Text(uiState.timer.phase.label, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                if (uiState.timer.isFaceDown) "Face down: Glyph active" else "Flip face down to focus",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NothingButton(if (uiState.timer.isRunning) "Pause" else "Start", viewModel::toggleManual)
                NothingButton("End", viewModel::end)
            }
            Spacer(Modifier)
            Text(
                "Round ${uiState.timer.completedFocusRounds + 1} / long break every ${uiState.settings.longBreakInterval}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
