package com.example.mobilefinance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green700,
    onPrimary = Surface,
    primaryContainer = Green100,
    onPrimaryContainer = Green800,
    secondary = Green500,
    onSecondary = Surface,
    background = Background,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Background,
    onSurfaceVariant = InkMuted,
    outline = Outline,
    outlineVariant = Outline,
    error = ErrorRed,
    onError = Surface,
)

@Composable
fun MobileFinanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
