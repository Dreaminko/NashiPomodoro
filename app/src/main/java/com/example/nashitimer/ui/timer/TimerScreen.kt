package com.example.nashitimer.ui.timer

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nashitimer.R
import com.example.nashitimer.domain.model.TimerPhase
import com.example.nashitimer.ui.components.PageTitle
import com.example.nashitimer.ui.components.ProgressRing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
            verticalAlignment = Alignment.Top
        ) {
            Column {
                PageTitle(stringResource(R.string.nav_focus))
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.offset(y = (-7).dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.timer_open_settings)
                )
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
                        text = stringResource(uiState.timer.phase.labelRes).uppercase(),
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
                    stringResource(
                        if (uiState.timer.isRunning) {
                            R.string.timer_running_hint
                        } else {
                            R.string.timer_ready_hint
                        }
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.weight(0.6f))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                        stringResource(
                            if (uiState.timer.isFaceDown) {
                                R.string.timer_glyph_active
                            } else {
                                R.string.timer_flip_to_begin
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(
                            if (uiState.timer.isFaceDown) {
                                R.string.timer_screen_dimmed
                            } else {
                                R.string.timer_phone_position_hint
                            }
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 24.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalIconButton(
                    onClick = viewModel::end,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = stringResource(R.string.timer_end_session),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = viewModel::toggleManual,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (uiState.timer.isRunning) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (uiState.timer.isRunning) {
                                R.string.timer_pause_focus
                        } else {
                                R.string.timer_start_focus
                            }
                        ),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            LongPressSkipButton(
                onLongClick = viewModel::skip,
                enabled = uiState.timer.phase != TimerPhase.IDLE,
            )
        }

        Text(
            text = stringResource(
                R.string.timer_round_summary,
                uiState.timer.completedFocusRounds + 1,
                uiState.settings.longBreakInterval
            ),
            modifier = Modifier.padding(top = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LongPressSkipButton(
    onLongClick: () -> Unit,
    enabled: Boolean
) {
    val skipDescription = stringResource(R.string.timer_long_press_skip)
    val skipAction = stringResource(R.string.timer_skip_current_phase)
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    val progressColor = MaterialTheme.colorScheme.primary
    val progress = remember { Animatable(0f) }
    var isPressing by remember { mutableStateOf(false) }
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    Box(
        modifier = Modifier
            .size(72.dp)
            .semantics {
                role = Role.Button
                contentDescription = skipDescription
                if (enabled) {
                    onLongClick(skipAction) {
                        currentOnLongClick()
                        true
                    }
                } else {
                    disabled()
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                coroutineScope {
                    val animationScope = this
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressing = true
                        var completed = false
                        val fillJob = animationScope.launch {
                            progress.snapTo(0f)
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 900,
                                    easing = LinearEasing
                                )
                            )
                            completed = true
                            currentOnLongClick()
                        }
                        waitForUpOrCancellation()
                        if (!completed) fillJob.cancel()
                        isPressing = false
                        animationScope.launch {
                            if (completed) {
                                progress.snapTo(0f)
                            } else {
                                progress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 150)
                                )
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(72.dp)) {
            if (!isPressing) return@Canvas
            val stroke = 4.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            if (progress.value > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = progress.value * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) containerColor else containerColor.copy(alpha = 0.38f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun phaseColor(phase: TimerPhase): Color = when (phase) {
    TimerPhase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
    TimerPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

private val TimerPhase.labelRes: Int
    get() = when (this) {
        TimerPhase.IDLE -> R.string.timer_ready
        TimerPhase.FOCUS -> R.string.timer_focus
        TimerPhase.SHORT_BREAK -> R.string.timer_short_break
        TimerPhase.LONG_BREAK -> R.string.timer_long_break
        TimerPhase.PAUSED -> R.string.timer_paused
    }
