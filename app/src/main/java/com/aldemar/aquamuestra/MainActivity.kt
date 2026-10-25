package com.aldemar.aquamuestra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.aldemar.aquamuestra.ui.screens.HomeScreen
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.form.NewSampleFormScreen
import com.aldemar.aquamuestra.ui.theme.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AquaMuestraTheme {

                // 1. Guardamos qué pantalla se muestra: "login", "home" o "nueva".
                var pantalla by rememberSaveable { mutableStateOf("login") }

                // 2. Guardamos los datos del usuario que inició sesión.
                var nombre by rememberSaveable { mutableStateOf("") }
                var rol by rememberSaveable { mutableStateOf("") }

                // 3. Según el valor de pantalla, mostramos una u otra.
                when (pantalla) {
                    "login" -> LoginScreen(
                        onLoginClick = { usuario, _, role, _ ->
                            nombre = usuario
                            rol = role.title
                            pantalla = "home"
                        }
                    )

                    "home" -> HomeScreen(
                        nombre = nombre,
                        rol = rol,
                        puedeCrear = rol == "Operador de Muestreo",
                        onNuevaMuestra = { pantalla = "nueva" },
                        onCerrarSesion = { pantalla = "login" }
                    )

                    else -> NewSampleFormScreen(
                        onBackClick = { pantalla = "home" }
                    )
                }
            }
        }
    }
}
