package com.aldemar.aquamuestra

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.aldemar.aquamuestra.data.sync.SyncScheduler
import com.aldemar.aquamuestra.ui.theme.AppNavigation
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        // Empiezan a leer DataStore (sesión y modo oscuro) en segundo plano.
        SessionManager.init(applicationContext)
        ThemeManager.init(applicationContext)

        setContent {
            // Al abrir la app con sesión guardada: sincronizar ahora y dejar la sincronización periódica.
            LaunchedEffect(SessionManager.cargado) {
                if (SessionManager.cargado && SessionManager.isLoggedIn) {
                    val sync = SyncScheduler.getInstance(applicationContext)
                    sync.sincronizarAhora()
                    sync.programarPeriodico()
                }
            }

            // Hasta que DataStore responda (unos milisegundos) no se dibuja nada: así no aparece
            // el Login un instante a alguien que ya tiene sesión, ni el tema claro antes del oscuro.
            if (SessionManager.cargado && ThemeManager.cargado) {
                AquaMuestraTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation() // NavHost con todas las rutas de la app
                    }
                }
            }
        }
    }
}
