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
    onSecondary = Color(0xFF003826),
    secondary = BreakMint,
    secondaryContainer = Color(0xFF153D2E),
    onSecondaryContainer = Color(0xFFAAF4D2),
    tertiary = LongBreakBlue,
    tertiaryContainer = Color(0xFF173640),
    onTertiaryContainer = Color(0xFFC5EAF7),
    background = NothingBlack,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceContainer,
    surfaceContainerLowest = Color(0xFF0F0E0C),
    surfaceContainerLow = Color(0xFF1E1C19),
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = Color(0xFF302D29),
    surfaceContainerHighest = Color(0xFF3B3834),
    outline = Color(0xFF4B4640),
    outlineVariant = Color(0xFF4B4640),
    onPrimary = NothingBlack,
    onPrimaryContainer = Color(0xFFFFDBC4),
    onBackground = NothingWhite,
    onSurface = NothingWhite,
    onSurfaceVariant = NothingGray
)

private val LightColorScheme = lightColorScheme(
    primary = FocusWarmDark,
    primaryContainer = Color(0xFFFFDBC4),
    onSecondary = Color.White,
    secondary = BreakMintDark,
    secondaryContainer = Color(0xFFB0F1D1),
    onSecondaryContainer = Color(0xFF002116),
    tertiary = Color(0xFF315A69),
    tertiaryContainer = Color(0xFFC5EAF7),
    onTertiaryContainer = Color(0xFF001F28),
    background = LightSurface,
    surface = NothingWhite,
    surfaceVariant = LightSurfaceContainer,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF1E7),
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = Color(0xFFEDE4DC),
    surfaceContainerHighest = Color(0xFFE6DED6),
    outline = Color(0xFFCFC4BA),
    outlineVariant = Color(0xFFD8CFC6),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFF321200),
    onBackground = NothingBlack,
    onSurface = NothingBlack,
    onSurfaceVariant = Color(0xFF6F675F)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
