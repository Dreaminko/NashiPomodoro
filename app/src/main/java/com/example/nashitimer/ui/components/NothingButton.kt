package com.example.nashitimer.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NothingButton(
    text: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    val contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    if (primary) {
        Button(
            onClick = onClick,
            modifier = modifier.defaultMinSize(minHeight = 54.dp),
            shape = MaterialTheme.shapes.extraLarge,
            contentPadding = contentPadding
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.defaultMinSize(minHeight = 54.dp),
            shape = MaterialTheme.shapes.extraLarge,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            contentPadding = contentPadding
        ) {
            Text(text)
        }
    }
}
