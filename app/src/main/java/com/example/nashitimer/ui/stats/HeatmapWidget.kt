package com.example.nashitimer.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HeatmapWidget(counts: List<Int>, modifier: Modifier = Modifier) {
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val low = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
    val medium = MaterialTheme.colorScheme.secondary
    val high = MaterialTheme.colorScheme.primary
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        counts.takeLast(28).forEach { count ->
            val color = when {
                count >= 6 -> high
                count >= 3 -> medium
                count >= 1 -> low
                else -> empty
            }
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(0.7f)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}
