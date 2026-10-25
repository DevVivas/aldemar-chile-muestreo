package com.aldemar.aquamuestra.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

object ThemeManager {
    private const val PREFS_NAME = "aquamuestra_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    private var prefs: SharedPreferences? = null
    private val _isDarkMode = mutableStateOf(false)

    /**
     * Estado del modo oscuro. Al asignarlo se guarda en el dispositivo,
     * por lo que se mantiene hasta que el usuario lo cambie de nuevo.
     */
    var isDarkMode: Boolean
        get() = _isDarkMode.value
        set(value) {
            _isDarkMode.value = value
            prefs?.edit()?.putBoolean(KEY_DARK_MODE, value)?.apply()
        }

    /** Carga la preferencia guardada. Es seguro llamarla varias veces. */
    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        _isDarkMode.value = p.getBoolean(KEY_DARK_MODE, false)
    }

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
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // Carga la preferencia guardada ANTES de leer el estado, para evitar un destello de tema claro
    val context = LocalContext.current
    remember { ThemeManager.init(context) }

    val isDark = darkTheme ?: ThemeManager.isDarkMode
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
