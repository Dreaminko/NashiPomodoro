package com.example.nashitimer.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HeatmapWidget(counts: List<Int>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        counts.takeLast(28).forEach { count ->
            val color = when {
                count >= 6 -> Color(0xFFFFD5B8)
                count >= 3 -> Color(0xFFB8E6D0)
                count >= 1 -> Color(0xFFC4D7E0)
                else -> Color(0xFF2A2A2A)
            }
            Box(Modifier.size(14.dp).background(color, RoundedCornerShape(4.dp)))
        }
    }
}
