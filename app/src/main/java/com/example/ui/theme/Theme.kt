package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color.Black,
    primaryContainer = SlateCard,
    onPrimaryContainer = Color.White,
    secondary = TealAccent,
    onSecondary = Color.Black,
    tertiary = BlueAccent,
    background = DarkNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = SlateCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = SlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = TealAccent,
    onSecondary = Color.White,
    tertiary = BlueAccent,
    background = LightBackground,
    onBackground = DarkText,
    surface = LightSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = MutedText,
    outline = LightBorder
)

@Composable
fun WidgetStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WidgetStudioTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
