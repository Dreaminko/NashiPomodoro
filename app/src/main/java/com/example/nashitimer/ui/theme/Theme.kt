package com.example.nashitimer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FocusWarm,
    secondary = BreakMint,
    tertiary = LongBreakBlue,
    background = NothingBlack,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF2B2B2B),
    onPrimary = NothingBlack,
    onBackground = NothingWhite,
    onSurface = NothingWhite,
    onSurfaceVariant = NothingGray
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8B3F18),
    secondary = Color(0xFF226B4C),
    tertiary = Color(0xFF315A69),
    background = LightSurface,
    surface = Color.White,
    surfaceVariant = Color(0xFFE5E5E0),
    onPrimary = Color.White,
    onBackground = NothingBlack,
    onSurface = NothingBlack,
    onSurfaceVariant = Color(0xFF555555)
)

@Composable
fun NashiTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
