package com.example.nashitimer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = FocusWarm,
    primaryContainer = Color(0xFF512306),
    secondary = BreakMint,
    secondaryContainer = Color(0xFF153D2E),
    tertiary = LongBreakBlue,
    background = NothingBlack,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceContainer,
    surfaceContainer = DarkSurfaceContainer,
    outline = Color(0xFF4B4640),
    onPrimary = NothingBlack,
    onPrimaryContainer = Color(0xFFFFDBC4),
    onBackground = NothingWhite,
    onSurface = NothingWhite,
    onSurfaceVariant = NothingGray
)

private val LightColorScheme = lightColorScheme(
    primary = FocusWarmDark,
    primaryContainer = Color(0xFFFFDBC4),
    secondary = BreakMintDark,
    secondaryContainer = Color(0xFFB0F1D1),
    tertiary = Color(0xFF315A69),
    background = LightSurface,
    surface = NothingWhite,
    surfaceVariant = LightSurfaceContainer,
    surfaceContainer = LightSurfaceContainer,
    outline = Color(0xFFCFC4BA),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFF321200),
    onBackground = NothingBlack,
    onSurface = NothingBlack,
    onSurfaceVariant = Color(0xFF6F675F)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun NashiTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        shapes = AppShapes,
        content = content
    )
}
