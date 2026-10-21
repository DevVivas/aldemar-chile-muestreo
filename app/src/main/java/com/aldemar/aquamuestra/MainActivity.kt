package com.aldemar.aquamuestra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.form.NewSampleFormScreen
import com.aldemar.aquamuestra.ui.theme.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AquaMuestraTheme {
                var currentScreen by remember { mutableStateOf("login") }

                if (currentScreen == "login") {
                    LoginScreen(
                        onLoginClick = { _, _, _, _ ->
                            currentScreen = "new_sample"
                        }
                    )
                } else {
                    NewSampleFormScreen(
                        onBackClick = {
                            currentScreen = "login"
                        }
                    )
                }
            }
        }
    }
}
