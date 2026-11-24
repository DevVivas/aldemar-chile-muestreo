package com.aldemar.aquamuestra.data.remote

import com.aldemar.aquamuestra.util.Fixtures
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class ResultadoTest {

    @Test
    fun `dada una llamada exitosa cuando se ejecuta entonces devuelve Ok con los datos`() = runTest {
        assertEquals(Resultado.Ok(7), llamarApi { 7 })
    }

    @Test
    fun `dado un error HTTP con JSON del backend cuando se ejecuta entonces trae codigo y mensaje`() = runTest {
        // Act
        val r = llamarApi<Int> { throw Fixtures.httpError(409, """{"status":409,"mensaje":"La muestra ya fue validada"}""") }

        // Assert
        assertEquals(Resultado.ErrorHttp(409, "La muestra ya fue validada"), r)
    }

    @Test
    fun `dado un error HTTP con cuerpo que no es JSON cuando se ejecuta entonces trae solo el codigo`() = runTest {
        // Act
        val r = llamarApi<Int> { throw Fixtures.httpError(500, "<html>error</html>") } as Resultado.ErrorHttp

        // Assert
        assertEquals(500, r.codigo)
        assertNull(r.mensaje)
    }

    @Test
    fun `dado un error de red cuando se ejecuta entonces devuelve sin conexion`() = runTest {
        assertEquals(Resultado.SinConexion, llamarApi<Int> { throw IOException("timeout") })
    }

    @Test(expected = CancellationException::class)
    fun `dada una cancelacion cuando se ejecuta entonces no se oculta y se relanza`() = runTest {
        llamarApi<Int> { throw CancellationException("cancelada") }
    }
}
