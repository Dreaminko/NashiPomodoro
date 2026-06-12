package com.example.nashitimer.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.nashitimer.ui.theme.NotoSerifJapanese

@Composable
fun PageTitle(
    text: String,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    val titleFontFamily =
        if (LocalConfiguration.current.locales[0].language == "ja") {
            NotoSerifJapanese
        } else {
            FontFamily.Serif
        }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        fontFamily = titleFontFamily,
        fontWeight = FontWeight(255)
    )
}
