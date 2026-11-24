package com.aldemar.aquamuestra.data.sync

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraDao
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.remote.AquaSampleApi
import com.aldemar.aquamuestra.data.remote.FotoSubidaDto
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.util.Fixtures
import com.aldemar.aquamuestra.util.Fixtures.muestra
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException

class SincronizadorMuestrasTest {

    // API (Retrofit) y DAO (Room) son mocks; el Repository y el Sincronizador son los reales.
    private val api = mockk<AquaSampleApi>()
    private val dao = mockk<MuestraDao>(relaxed = true)
    private val repo = MuestraRepository(dao)
    private val logs = mutableListOf<String>()
    private var temporalComprimido: File? = null

    private fun sincronizador(comprimir: (File) -> File? = { original ->
        File.createTempFile("comprimida", ".jpg").also { it.writeText("jpg"); temporalComprimido = it }
    }) = SincronizadorMuestras(api, repo, comprimir, log = { logs += it })

    private fun pendientes(vararg m: MuestraEntity) = coEvery { dao.obtenerPendientesSync() } returns m.toList()

    private fun sinNadaQueBajar() = coEvery { api.listarMuestras(any(), any(), any(), any(), any(), any()) } returns emptyList()

    @Test
    fun `dada una muestra pendiente sin foto cuando se sincroniza entonces se sube y se marca sincronizada`() = runTest {
        // Arrange
        val m = muestra(id = "a", actualizadoEn = 77L)
        pendientes(m)
        coEvery { api.guardarMuestra("a", any()) } returns Fixtures.dto(id = "a")
        sinNadaQueBajar()

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        coVerify { dao.marcarSincronizadaSiNoCambio("a", 77L) }
        coVerify(exactly = 0) { api.subirFoto(any(), any()) }
    }

    @Test
    fun `dada una muestra con foto cuando se sincroniza entonces sube la foto comprimida y borra el temporal`() = runTest {
        // Arrange
        val foto = File.createTempFile("original", ".jpg").apply { writeText("foto") }
        pendientes(muestra(id = "a", fotoPath = foto.absolutePath))
        coEvery { api.guardarMuestra("a", any()) } returns Fixtures.dto(id = "a")
        coEvery { api.subirFoto("a", any()) } returns FotoSubidaDto("/api/muestras/a/foto")
        sinNadaQueBajar()

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        coVerify { api.subirFoto("a", any()) }
        coVerify { dao.marcarSincronizadaSiNoCambio("a", any()) }
        assertFalse(temporalComprimido!!.exists())
        assertTrue(foto.exists()) // la original del teléfono no se toca
    }

    @Test
    fun `dada una foto que no se puede leer cuando se sincroniza entonces la muestra queda pendiente`() = runTest {
        // Arrange
        val foto = File.createTempFile("danada", ".jpg")
        pendientes(muestra(id = "a", fotoPath = foto.absolutePath))
        coEvery { api.guardarMuestra("a", any()) } returns Fixtures.dto(id = "a")
        sinNadaQueBajar()

        // Act
        val r = sincronizador(comprimir = { null }).ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        coVerify(exactly = 0) { dao.marcarSincronizadaSiNoCambio(any(), any()) }
        assertTrue(logs.any { "no se pudo leer la foto" in it })
    }

    @Test
    fun `dado que la foto responde 400 cuando se sincroniza entonces la muestra queda pendiente y se sigue`() = runTest {
        // Arrange
        val foto = File.createTempFile("original", ".jpg")
        pendientes(muestra(id = "a", fotoPath = foto.absolutePath))
        coEvery { api.guardarMuestra("a", any()) } returns Fixtures.dto(id = "a")
        coEvery { api.subirFoto("a", any()) } throws Fixtures.httpError(400)
        sinNadaQueBajar()

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        coVerify(exactly = 0) { dao.marcarSincronizadaSiNoCambio(any(), any()) }
    }

    @Test
    fun `dado que la foto falla por red o 5xx cuando se sincroniza entonces pide reintentar`() = runTest {
        // Arrange
        val foto = File.createTempFile("original", ".jpg")
        pendientes(muestra(id = "a", fotoPath = foto.absolutePath))
        coEvery { api.guardarMuestra("a", any()) } returns Fixtures.dto(id = "a")

        // Act + Assert: sin red
        coEvery { api.subirFoto("a", any()) } throws IOException()
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())

        // Act + Assert: error del servidor
        coEvery { api.subirFoto("a", any()) } throws Fixtures.httpError(503)
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())
    }

    @Test
    fun `dado un 409 porque ya esta validada cuando se sincroniza entonces se reemplaza por la del servidor`() = runTest {
        // Arrange
        pendientes(muestra(id = "a", estado = EstadoRevision.ENVIADA, fotoPath = "/fotos/a.jpg"))
        coEvery { api.guardarMuestra("a", any()) } throws Fixtures.httpError(409)
        coEvery { api.obtenerMuestra("a") } returns Fixtures.dto(id = "a", estado = EstadoRevision.VALIDADA, revisadoPor = "Supervisor Demo")
        sinNadaQueBajar()
        val guardada = slot<MuestraEntity>()
        coEvery { dao.upsert(capture(guardada)) } returns Unit

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        assertEquals(EstadoRevision.VALIDADA, guardada.captured.estado)
        assertFalse(guardada.captured.pendienteSync)
        assertEquals("/fotos/a.jpg", guardada.captured.fotoPath) // la foto local se conserva
    }

    @Test
    fun `dado un 409 y el GET falla cuando se sincroniza entonces reintenta si es red y sigue si es 404`() = runTest {
        // Arrange
        pendientes(muestra(id = "a"))
        coEvery { api.guardarMuestra("a", any()) } throws Fixtures.httpError(409)
        sinNadaQueBajar()

        // Act + Assert: GET sin red → reintentar
        coEvery { api.obtenerMuestra("a") } throws IOException()
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())

        // Act + Assert: GET con 500 → reintentar
        coEvery { api.obtenerMuestra("a") } throws Fixtures.httpError(500)
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())

        // Act + Assert: GET con 404 → no se arregla reintentando, se sigue
        coEvery { api.obtenerMuestra("a") } throws Fixtures.httpError(404)
        assertEquals(ResultadoSync.COMPLETO, sincronizador().ejecutar())
    }

    @Test
    fun `dado un 403 en una muestra cuando se sincroniza entonces queda pendiente y se sigue con la siguiente`() = runTest {
        // Arrange
        pendientes(muestra(id = "prohibida"), muestra(id = "buena", actualizadoEn = 8L))
        coEvery { api.guardarMuestra("prohibida", any()) } throws Fixtures.httpError(403)
        coEvery { api.guardarMuestra("buena", any()) } returns Fixtures.dto(id = "buena")
        sinNadaQueBajar()

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        coVerify(exactly = 0) { dao.marcarSincronizadaSiNoCambio("prohibida", any()) }
        coVerify { dao.marcarSincronizadaSiNoCambio("buena", 8L) }
        assertTrue(logs.any { "403" in it })
    }

    @Test
    fun `dado un error de red al subir cuando se sincroniza entonces pide reintentar y no baja nada`() = runTest {
        // Arrange
        pendientes(muestra(id = "a"))
        coEvery { api.guardarMuestra("a", any()) } throws IOException()

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.REINTENTAR, r)
        coVerify(exactly = 0) { api.listarMuestras(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `dado un error 500 al subir cuando se sincroniza entonces pide reintentar`() = runTest {
        // Arrange
        pendientes(muestra(id = "a"))
        coEvery { api.guardarMuestra("a", any()) } throws Fixtures.httpError(500)

        // Act + Assert
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())
    }

    @Test
    fun `dadas muestras en el servidor cuando se baja entonces no pisa las pendientes locales y conserva la foto local`() = runTest {
        // Arrange: "a" sigue pendiente en el teléfono, "b" ya existe sincronizada con foto, "c" es nueva
        pendientes()
        coEvery { dao.obtenerPorId("a") } returns muestra(id = "a", pendienteSync = true)
        coEvery { dao.obtenerPorId("b") } returns muestra(id = "b", pendienteSync = false, fotoPath = "/fotos/b.jpg", marcasJson = "[]")
        coEvery { dao.obtenerPorId("c") } returns null
        coEvery { api.listarMuestras(any(), any(), any(), any(), any(), any()) } returns
            listOf(Fixtures.dto(id = "a"), Fixtures.dto(id = "b"), Fixtures.dto(id = "c"))
        val guardadas = mutableListOf<MuestraEntity>()
        coEvery { dao.upsert(capture(guardadas)) } returns Unit

        // Act
        val r = sincronizador().ejecutar()

        // Assert
        assertEquals(ResultadoSync.COMPLETO, r)
        assertEquals(listOf("b", "c"), guardadas.map { it.id })
        assertEquals("/fotos/b.jpg", guardadas[0].fotoPath)
        assertEquals("[]", guardadas[0].marcasJson)
    }

    @Test
    fun `dado que bajar falla cuando se sincroniza entonces reintenta si es red o 5xx y sigue si es 401`() = runTest {
        // Arrange
        pendientes()

        // Act + Assert: sin red
        coEvery { api.listarMuestras(any(), any(), any(), any(), any(), any()) } throws IOException()
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())

        // Act + Assert: 502
        coEvery { api.listarMuestras(any(), any(), any(), any(), any(), any()) } throws Fixtures.httpError(502)
        assertEquals(ResultadoSync.REINTENTAR, sincronizador().ejecutar())

        // Act + Assert: 401 (credenciales vencidas): no se reintenta en bucle, se registra
        coEvery { api.listarMuestras(any(), any(), any(), any(), any(), any()) } throws Fixtures.httpError(401)
        assertEquals(ResultadoSync.COMPLETO, sincronizador().ejecutar())
        assertTrue(logs.any { "No se pudieron bajar" in it })
    }
}
