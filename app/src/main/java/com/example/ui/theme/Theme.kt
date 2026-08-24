package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF002B36),
    primaryContainer = Color(0xFF0D3848),
    onPrimaryContainer = Color(0xFF80F0FF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF2E0854),
    secondaryContainer = Color(0xFF381E72),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = NeonAmber,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF633F00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    background = DarkBg,
    onBackground = Color(0xFFF0F6FC),
    surface = DarkSurface,
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFF8B949E),
    outline = DarkBorder,
    outlineVariant = Color(0xFF21262D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightMutedText,
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun WidgetStudioTheme(
    darkTheme: Boolean = true, // Default to sleek dark inspired by luxury dashboard UI
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
        typography = Typography
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WidgetStudioTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
