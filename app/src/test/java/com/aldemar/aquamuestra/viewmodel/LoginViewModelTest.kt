package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.Sesion
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.UsuarioDto
import com.aldemar.aquamuestra.data.repository.AuthRepository
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = mockk<AuthRepository>()
    private val sesionesGuardadas = mutableListOf<Sesion>()
    private val vm = LoginViewModel(auth, guardarSesion = { sesionesGuardadas += it })

    private fun escribir(usuario: String, password: String) {
        vm.onUsuarioChange(usuario)
        vm.onPasswordChange(password)
        every { auth.credencialBasic(any(), any()) } returns "Basic x"
    }

    @Test
    fun `dados campos vacios cuando se inicia sesion entonces marca ambos y no llama al servidor`() = runTest {
        // Act
        vm.iniciarSesion()

        // Assert
        assertEquals(ErrorLogin.USUARIO_VACIO, vm.uiState.value.errorUsuario)
        assertEquals(ErrorLogin.PASSWORD_VACIA, vm.uiState.value.errorPassword)
        coVerify(exactly = 0) { auth.quienSoy(any()) }
    }

    @Test
    fun `dadas credenciales correctas cuando se inicia sesion entonces guarda la sesion con el rol`() = runTest {
        // Arrange
        escribir(" op01 ", "clave")
        coEvery { auth.quienSoy("Basic x") } returns Resultado.Ok(UsuarioDto("op01", "Operario Demo", "OPERARIO"))

        // Act
        vm.iniciarSesion()

        // Assert
        assertTrue(vm.uiState.value.ingresoOk)
        assertFalse(vm.uiState.value.cargando)
        assertEquals(listOf(Sesion("op01", "Operario Demo", UserRole.OPERARIO, "Basic x")), sesionesGuardadas)
    }

    @Test
    fun `dadas credenciales incorrectas cuando se inicia sesion entonces marca 401 en ambos campos`() = runTest {
        // Arrange
        escribir("op01", "mala")
        coEvery { auth.quienSoy(any()) } returns Resultado.ErrorHttp(401, null)

        // Act
        vm.iniciarSesion()

        // Assert
        assertEquals(ErrorLogin.CREDENCIALES, vm.uiState.value.errorUsuario)
        assertEquals(ErrorLogin.CREDENCIALES, vm.uiState.value.errorPassword)
        assertTrue(sesionesGuardadas.isEmpty())

        // Act: al corregir la contraseña se quitan ambos errores
        vm.onPasswordChange("otra")

        // Assert
        assertNull(vm.uiState.value.errorUsuario)
        assertNull(vm.uiState.value.errorPassword)
    }

    @Test
    fun `dado que no hay red cuando se inicia sesion entonces avisa sin conexion`() = runTest {
        // Arrange
        escribir("op01", "clave")
        coEvery { auth.quienSoy(any()) } returns Resultado.SinConexion

        // Act
        vm.iniciarSesion()

        // Assert
        assertEquals(ErrorLogin.SIN_CONEXION, vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.ingresoOk)
    }

    @Test
    fun `dado un error 500 o un rol desconocido cuando se inicia sesion entonces avisa error del servidor`() = runTest {
        // Arrange
        escribir("op01", "clave")

        // Act + Assert: 500
        coEvery { auth.quienSoy(any()) } returns Resultado.ErrorHttp(500, null)
        vm.iniciarSesion()
        assertEquals(ErrorLogin.SERVIDOR, vm.uiState.value.errorGeneral)

        // Act + Assert: rol que la app no conoce
        coEvery { auth.quienSoy(any()) } returns Resultado.Ok(UsuarioDto("x", "X", "ADMIN"))
        vm.iniciarSesion()
        assertEquals(ErrorLogin.SERVIDOR, vm.uiState.value.errorGeneral)
        assertTrue(sesionesGuardadas.isEmpty())
    }

    @Test
    fun `dado que se nego el permiso de red local cuando se avisa entonces muestra ese error`() {
        // Act
        vm.onPermisoRedDenegado()

        // Assert
        assertEquals(ErrorLogin.SIN_PERMISO_RED, vm.uiState.value.errorGeneral)

        // Act: al escribir se limpia
        vm.onUsuarioChange("op01")

        // Assert
        assertNull(vm.uiState.value.errorGeneral)
    }
}
