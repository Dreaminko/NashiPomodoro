package com.example.nashitimer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    diameter: Dp = 276.dp,
    strokeWidth: Dp = 12.dp
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier = modifier.size(diameter)) {
        val stroke = strokeWidth.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = progress.coerceIn(0f, 1f) * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
    }
}
