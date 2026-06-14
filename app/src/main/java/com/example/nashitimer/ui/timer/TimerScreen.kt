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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageTitle(
                text = stringResource(R.string.nav_focus),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.timer_open_settings),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        uiState.activeTask?.let { task ->
            Surface(
                modifier = Modifier
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape
            ) {
                Text(
                    text = stringResource(R.string.timer_active_task, task.title),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.weight(0.45f))

        Box(contentAlignment = Alignment.Center) {
            ProgressRing(
                progress = progress,
                activeColor = accent,
                diameter = 264.dp,
                strokeWidth = 10.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .background(accent, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(uiState.timer.phase.labelRes).uppercase(),
                        color = accent,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(10.dp))
                CountdownTime(uiState.timer.timeText)
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

        Spacer(Modifier.weight(0.4f))

        GlyphStatus(
            isFaceDown = uiState.timer.isFaceDown,
            glyphProgressEnabled = uiState.settings.glyphProgressEnabled,
            accent = accent
        )

        Text(
            text = stringResource(
                R.string.timer_round_summary,
                uiState.timer.completedFocusRounds + 1,
                uiState.settings.longBreakInterval
            ),
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 22.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = viewModel::end,
                enabled = uiState.timer.phase != TimerPhase.IDLE,
                modifier = Modifier.size(56.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = stringResource(R.string.timer_end_session),
                    modifier = Modifier.size(25.dp)
                )
            }
            FilledIconButton(
                onClick = viewModel::toggleManual,
                modifier = Modifier.size(72.dp),
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
                    modifier = Modifier.size(32.dp)
                )
            }
            LongPressSkipButton(
                onLongClick = viewModel::skip,
                enabled = uiState.timer.phase != TimerPhase.IDLE,
            )
        }
    }
}

@Composable
private fun CountdownTime(timeText: String) {
    // Adjust this value to move the colon down (positive) or up (negative).
    val colonVerticalOffset = (-4).dp
    val parts = timeText.split(':', limit = 2)
    val minutes = parts.firstOrNull().orEmpty()
    val seconds = parts.getOrNull(1).orEmpty()
    val style = MaterialTheme.typography.displayLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        letterSpacing = 1.sp,
        fontFeatureSettings = "tnum"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = minutes,
            style = style
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = ":",
            modifier = Modifier.offset(y = colonVerticalOffset),
            style = style
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = seconds,
            style = style
        )
    }
}

@Composable
private fun GlyphStatus(
    isFaceDown: Boolean,
    glyphProgressEnabled: Boolean,
    accent: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(
                        if (isFaceDown) accent else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    stringResource(
                        if (isFaceDown) {
                            if (glyphProgressEnabled) {
                                R.string.timer_glyph_active
                            } else {
                                R.string.timer_flip_focus_active
                            }
                        } else {
                            R.string.timer_flip_to_begin
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    stringResource(
                        if (isFaceDown) {
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
            .size(60.dp)
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
        Canvas(Modifier.size(60.dp)) {
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
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) containerColor else containerColor.copy(alpha = 0.38f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = null,
                modifier = Modifier.size(25.dp),
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
