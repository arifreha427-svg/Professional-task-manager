package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = CyberIndigo,
    tertiary = VividRose,
    background = ObsidianDark,
    surface = SlateCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SlateBorder,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCyan,
    secondary = CyberIndigo,
    tertiary = VividRose,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBorder,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for modern portfolio aesthetic by default!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
