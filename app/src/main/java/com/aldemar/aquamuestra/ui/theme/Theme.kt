package com.aldemar.aquamuestra.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aldemar.aquamuestra.data.local.PreferenciasStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Solo guarda la preferencia de modo oscuro. Los colores salen de MaterialTheme.colorScheme. */
object ThemeManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var store: PreferenciasStore? = null
    private val _isDarkMode = mutableStateOf(false)

    /** true cuando ya se leyó la preferencia de DataStore (evita mostrar un tema y cambiar al otro). */
    var cargado by mutableStateOf(false)
        private set

    /**
     * Estado del modo oscuro. Al asignarlo se guarda en DataStore,
     * por lo que se mantiene hasta que el usuario lo cambie de nuevo.
     */
    var isDarkMode: Boolean
        get() = _isDarkMode.value
        set(value) {
            _isDarkMode.value = value
            scope.launch { store?.guardarModoOscuro(value) }
        }

    /** Empieza a escuchar la preferencia guardada. Es seguro llamarla varias veces. */
    fun init(context: Context) {
        if (store != null) return
        val s = PreferenciasStore.getInstance(context)
        store = s
        scope.launch {
            s.modoOscuro.collect {
                _isDarkMode.value = it
                cargado = true
            }
        }
    }
}

// Paleta marina de la app: azul marino #003366 y teal #008080 (README), con tonos derivados
// para Material 3. Fondo blanco / #121212.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA4C9F0), onPrimary = Color(0xFF00315A),
    primaryContainer = Color(0xFF0D4A7A), onPrimaryContainer = Color(0xFFD4E3F7),
    secondary = Color(0xFF80D5D3), onSecondary = Color(0xFF003738),
    secondaryContainer = Color(0xFF0F4F50), onSecondaryContainer = Color(0xFFCDEBEA),
    tertiary = Color(0xFFFFB869), onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF6A3A00), onTertiaryContainer = Color(0xFFFFDDBA),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212), onBackground = Color(0xFFE2E8EE),
    surface = Color(0xFF121212), onSurface = Color(0xFFE2E8EE),
    surfaceVariant = Color(0xFF3F4852), onSurfaceVariant = Color(0xFFBFC8D2),
    surfaceTint = Color(0xFFA4C9F0),
    inverseSurface = Color(0xFFE2E8EE), inverseOnSurface = Color(0xFF2C3440), inversePrimary = Color(0xFF003366),
    outline = Color(0xFF89939D), outlineVariant = Color(0xFF3F4852),
    surfaceBright = Color(0xFF383B40), surfaceDim = Color(0xFF121212),
    surfaceContainerLowest = Color(0xFF0C0F12), surfaceContainerLow = Color(0xFF1A1D21),
    surfaceContainer = Color(0xFF1E2226), surfaceContainerHigh = Color(0xFF282C31),
    surfaceContainerHighest = Color(0xFF33373C)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF003366), onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3F7), onPrimaryContainer = Color(0xFF0A2440),
    secondary = Color(0xFF00696B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEBEA), onSecondaryContainer = Color(0xFF0B3A3B),
    tertiary = Color(0xFF8A4B00), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDBA), onTertiaryContainer = Color(0xFF2D1600),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    background = Color.White, onBackground = Color(0xFF1A2330),
    surface = Color.White, onSurface = Color(0xFF1A2330),
    surfaceVariant = Color(0xFFDEE4EB), onSurfaceVariant = Color(0xFF434E5A),
    surfaceTint = Color(0xFF003366),
    inverseSurface = Color(0xFF2C3440), inverseOnSurface = Color(0xFFEDF1F6), inversePrimary = Color(0xFFA4C9F0),
    outline = Color(0xFF6F7A86), outlineVariant = Color(0xFFC3CBD4),
    surfaceBright = Color.White, surfaceDim = Color(0xFFD8DEE4),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF3F6F9),
    surfaceContainer = Color(0xFFEDF1F5), surfaceContainerHigh = Color(0xFFE7ECF1),
    surfaceContainerHighest = Color(0xFFE1E7ED)
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
