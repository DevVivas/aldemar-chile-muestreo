package com.aldemar.aquamuestra

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aldemar.aquamuestra.ui.theme.AppNavigation
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme

// Asegúrate de que el paquete coincida con el tuyo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // AquaMuestraTheme escuchará los cambios globales de ThemeManager.isDarkMode
            AquaMuestraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Tu NavHost con todas las rutas de la app
                }
            }
        }
    }
}