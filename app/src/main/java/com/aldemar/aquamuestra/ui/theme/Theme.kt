package com.aldemar.aquamuestra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemeManager {
    var isDarkMode by mutableStateOf(false)

    // Getters dinámicos para utilizar en cualquier composable de la app
    val backgroundColor: Color
        @Composable get() = if (isDarkMode) ColorDarkBackground else ColorPaper

    val surfaceColor: Color
        @Composable get() = if (isDarkMode) ColorDarkSurface else ColorFog

    val textColor: Color
        @Composable get() = if (isDarkMode) ColorDarkTextPrimary else ColorInk

    val secondaryTextColor: Color
        @Composable get() = if (isDarkMode) ColorDarkTextSecondary else ColorGraphite

    val borderColor: Color
        @Composable get() = if (isDarkMode) ColorDarkBorder else ColorMist
}

private val DarkColorScheme = darkColorScheme(
    primary = ColorEmber,
    background = ColorDarkBackground,
    surface = ColorDarkSurface,
    surfaceVariant = ColorDarkSurface,
    onBackground = ColorDarkTextPrimary,
    onSurface = ColorDarkTextPrimary,
    outline = ColorDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ColorEmber,
    background = ColorPaper,
    surface = ColorPaper,
    surfaceVariant = ColorFog,
    onBackground = ColorInk,
    onSurface = ColorInk,
    outline = ColorMist
)

@Composable
fun AquaMuestraTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}