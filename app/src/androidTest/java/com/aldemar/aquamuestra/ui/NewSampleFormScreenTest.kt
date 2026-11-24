package com.aldemar.aquamuestra.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.form.NewSampleFormScreen
import com.aldemar.aquamuestra.ui.theme.form.TAG_CAMPO_TRAMO
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de interfaz del formulario "Nueva muestra" en el emulador.
 * Usa la pantalla real (con su ViewModel); como el formulario no pasa la validación, no se guarda nada.
 */
@RunWith(AndroidJUnit4::class)
class NewSampleFormScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Before
    fun abrirFormulario() {
        compose.setContent { AquaMuestraTheme { NewSampleFormScreen() } }
    }

    /** El botón está en la barra inferior fija (fuera del scroll), así que se toca directo. */
    private fun tocarEnviar() =
        compose.onNode(hasText("Enviar a revisión") and hasClickAction()).performClick()

    private fun verMensaje(texto: String) =
        compose.onNodeWithText(texto).performScrollTo().assertIsDisplayed()

    @Test
    fun dadoElFormularioVacio_cuandoSeTocaEnviar_entoncesAparecenLosErroresBajoLosObligatorios() {
        // Act
        tocarEnviar()

        // Assert
        verMensaje("Elige el centro de cultivo")
        verMensaje("Elige el tren")
        verMensaje("Elige la línea")
        verMensaje("No hay usuario en la sesión. Vuelve a iniciar sesión.")
        verMensaje("Escribe el largo del tramo en cm")
        verMensaje("Toma una foto para enviar a revisión")
    }

    @Test
    fun dadoUnTramoDeCero_cuandoSeEnvia_entoncesApareceElErrorDelTramo() {
        // Arrange
        compose.onNodeWithTag(TAG_CAMPO_TRAMO).performScrollTo().performTextInput("0")

        // Act
        tocarEnviar()

        // Assert
        verMensaje("El tramo debe ser mayor a 0 cm")
    }
}
