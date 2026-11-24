package com.aldemar.aquamuestra.data.remote.marine

import com.aldemar.aquamuestra.data.local.CentrosCatalogo
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.util.Fixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class MarineRepositoryTest {

    private val api = mockk<MarineApi>()
    private val cache = mockk<CacheMarino>(relaxed = true)
    private val repo = MarineRepository(api, cache, reloj = { 99L })
    private val calbuco = CentrosCatalogo.centros.first()

    private val respuesta = MarineRespuestaDto(
        latitude = -41.79, longitude = -73.12, timezone = "America/Santiago",
        current = MarineActualDto(
            time = "2026-11-24T01:45", alturaOla = 0.22, direccionOla = 251.0, periodoOla = 2.65,
            temperaturaMar = 15.8, velocidadCorriente = 0.4, direccionCorriente = 270.0
        )
    )

    @Test
    fun `dada una respuesta de Open-Meteo cuando se consulta entonces devuelve las condiciones y las guarda en cache`() = runTest {
        // Arrange
        coEvery { api.condicionesActuales(calbuco.latitud, calbuco.longitud, any(), any()) } returns respuesta

        // Act
        val r = repo.consultar(calbuco) as Resultado.Ok

        // Assert
        assertEquals(0.22, r.datos.alturaOlaM!!, 0.0)
        assertEquals("2026-11-24T01:45", r.datos.horaDato)
        assertEquals(99L, r.datos.consultadoEn)
        coVerify { cache.guardar(calbuco.nombre, r.datos) }
    }

    @Test
    fun `dada una respuesta sin current cuando se consulta entonces es un error y no se guarda`() = runTest {
        // Arrange
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } returns respuesta.copy(current = null)

        // Act
        val r = repo.consultar(calbuco)

        // Assert
        assert(r is Resultado.ErrorHttp)
        coVerify(exactly = 0) { cache.guardar(any(), any()) }
    }

    @Test
    fun `dado un error HTTP cuando se consulta entonces devuelve el codigo`() = runTest {
        // Arrange
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } throws Fixtures.httpError(400, "{}")

        // Act + Assert
        assertEquals(400, (repo.consultar(calbuco) as Resultado.ErrorHttp).codigo)
    }

    @Test
    fun `dado que no hay red cuando se consulta entonces devuelve sin conexion`() = runTest {
        // Arrange
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } throws IOException()

        // Act + Assert
        assertEquals(Resultado.SinConexion, repo.consultar(calbuco))
    }

    @Test
    fun `dado un dato guardado cuando se lee el cache entonces lo devuelve por nombre de centro`() = runTest {
        // Arrange
        val guardado = CondicionesMarinas(0.1, 10.0, 2.0, 15.0, 0.3, 90.0, "2026-11-24T01:00", 5L)
        coEvery { cache.leer(calbuco.nombre) } returns guardado

        // Act + Assert
        assertEquals(guardado, repo.leerCache(calbuco))
    }
}
