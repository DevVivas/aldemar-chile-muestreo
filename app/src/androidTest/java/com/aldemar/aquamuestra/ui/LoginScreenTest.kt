package com.aldemar.aquamuestra.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.login.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Login con campos vacíos: se valida en el teléfono, sin llamar al servidor. */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun dadosCamposVacios_cuandoSeTocaIniciarSesion_entoncesPideUsuarioYContrasena() {
        // Arrange
        compose.setContent { AquaMuestraTheme { LoginScreen() } }

        // Act
        compose.onNode(hasText("Iniciar sesión") and hasClickAction()).performScrollTo().performClick()

        // Assert
        compose.onNodeWithText("Escribe tu usuario").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Escribe tu contraseña").performScrollTo().assertIsDisplayed()
    }
}
