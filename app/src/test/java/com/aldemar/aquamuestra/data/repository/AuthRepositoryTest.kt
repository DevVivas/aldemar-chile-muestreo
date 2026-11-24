package com.aldemar.aquamuestra.data.repository

import com.aldemar.aquamuestra.data.remote.AquaSampleApi
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.UsuarioDto
import com.aldemar.aquamuestra.util.Fixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AuthRepositoryTest {

    private val api = mockk<AquaSampleApi>()
    private val repo = AuthRepository(api)

    @Test
    fun `dado usuario y clave cuando se arma la credencial entonces es Basic en Base64`() {
        // "op01:123" en Base64 es "b3AwMToxMjM="
        assertEquals("Basic b3AwMToxMjM=", repo.credencialBasic("op01", "123"))
    }

    @Test
    fun `dadas credenciales correctas cuando se pregunta quien soy entonces devuelve el usuario`() = runTest {
        // Arrange
        val usuario = UsuarioDto("op01", "Operario Demo", "OPERARIO")
        coEvery { api.me("Basic x") } returns usuario

        // Act
        val r = repo.quienSoy("Basic x")

        // Assert
        assertEquals(Resultado.Ok(usuario), r)
    }

    @Test
    fun `dadas credenciales incorrectas cuando se pregunta quien soy entonces devuelve error 401`() = runTest {
        // Arrange
        coEvery { api.me(any()) } throws Fixtures.httpError(401)

        // Act
        val r = repo.quienSoy("Basic mala")

        // Assert
        assertEquals(401, (r as Resultado.ErrorHttp).codigo)
    }

    @Test
    fun `dado que no hay red cuando se pregunta quien soy entonces devuelve sin conexion`() = runTest {
        // Arrange
        coEvery { api.me(any()) } throws IOException("sin red")

        // Act + Assert
        assertEquals(Resultado.SinConexion, repo.quienSoy("Basic x"))
    }
}
