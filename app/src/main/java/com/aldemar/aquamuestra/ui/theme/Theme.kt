package com.aldemar.aquamuestra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AquaColorScheme = lightColorScheme(
    primary = AquaNavyButton,
    onPrimary = AquaSurface,
    secondary = AquaTeal,
    background = AquaBackground,
    onBackground = AquaNavy,
    surface = AquaSurface,
    onSurface = AquaNavy,
    surfaceVariant = AquaSurfaceVariant,
    onSurfaceVariant = AquaTextSecondary,
    outline = AquaOutline
)

@Composable
fun AquaMuestraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AquaColorScheme,
        typography = AquaTypography,
        content = content
    )
}