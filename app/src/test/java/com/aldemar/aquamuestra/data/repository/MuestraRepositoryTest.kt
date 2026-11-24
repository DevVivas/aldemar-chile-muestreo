package com.aldemar.aquamuestra.data.repository

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraDao
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.sync.Sincronizador
import com.aldemar.aquamuestra.util.Fixtures.muestra
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuestraRepositoryTest {

    // MockK: objetos "de mentira" que reemplazan al DAO (Room) y al sincronizador (WorkManager)
    private val dao = mockk<MuestraDao>(relaxed = true)
    private val sincronizador = mockk<Sincronizador>(relaxed = true)
    private val reloj = { 42_000L } // hora fija: el resultado es siempre el mismo
    private val repo = MuestraRepository(dao, reloj, sincronizador)

    @Test
    fun `dada una muestra cuando se guarda entonces queda pendiente con la hora del reloj y avisa sincronizar`() = runTest {
        // Arrange
        val guardada = slot<MuestraEntity>()
        coEvery { dao.upsert(capture(guardada)) } returns Unit

        // Act
        repo.guardar(muestra(pendienteSync = false, actualizadoEn = 1L))

        // Assert
        assertTrue(guardada.captured.pendienteSync)
        assertEquals(42_000L, guardada.captured.actualizadoEn)
        verify(exactly = 1) { sincronizador.sincronizarAhora() }
    }

    @Test
    fun `dado un supervisor cuando valida entonces registra la revision sin comentario y avisa sincronizar`() = runTest {
        // Act
        repo.validar("m-1", "Supervisor Demo")

        // Assert
        coVerify { dao.registrarRevision("m-1", EstadoRevision.VALIDADA, null, "Supervisor Demo", 42_000L) }
        verify { sincronizador.sincronizarAhora() }
    }

    @Test
    fun `dado un comentario con espacios cuando se observa entonces se guarda recortado y avisa sincronizar`() = runTest {
        // Act
        repo.observar("m-1", "Supervisor Demo", "  Repetir conteo  ")

        // Assert
        coVerify { dao.registrarRevision("m-1", EstadoRevision.OBSERVADA, "Repetir conteo", "Supervisor Demo", 42_000L) }
        verify { sincronizador.sincronizarAhora() }
    }

    @Test
    fun `dado un cambio de estado cuando se aplica entonces usa el reloj y avisa sincronizar`() = runTest {
        // Act
        repo.cambiarEstado("m-1", EstadoRevision.BORRADOR)

        // Assert
        coVerify { dao.cambiarEstado("m-1", EstadoRevision.BORRADOR, 42_000L) }
        verify { sincronizador.sincronizarAhora() }
    }

    @Test
    fun `dada una muestra del servidor cuando se guarda entonces no la marca pendiente ni avisa sincronizar`() = runTest {
        // Arrange
        val delServidor = muestra(pendienteSync = false)

        // Act
        repo.guardarDesdeServidor(delServidor)

        // Assert
        coVerify { dao.upsert(delServidor) }
        verify(exactly = 0) { sincronizador.sincronizarAhora() }
    }

    @Test
    fun `dado que el DAO actualiza 1 o 0 filas cuando se marca sincronizada entonces devuelve true o false`() = runTest {
        // Arrange
        coEvery { dao.marcarSincronizadaSiNoCambio("a", 1L) } returns 1
        coEvery { dao.marcarSincronizadaSiNoCambio("b", 1L) } returns 0

        // Act + Assert
        assertTrue(repo.marcarSincronizadaSiNoCambio("a", 1L))
        assertFalse(repo.marcarSincronizadaSiNoCambio("b", 1L))
    }

    @Test
    fun `dadas consultas de lectura cuando se piden entonces se delegan en el DAO`() = runTest {
        // Arrange
        val m = muestra()
        every { dao.obtenerTodas() } returns flowOf(listOf(m))
        every { dao.observarPorId("m-1") } returns flowOf(m)
        every { dao.obtenerHistoricoLinea("C", "T", "L") } returns flowOf(listOf(m))
        every { dao.contarPendientes() } returns flowOf(3)
        coEvery { dao.obtenerPorId("m-1") } returns m
        coEvery { dao.obtenerPendientesSync() } returns listOf(m)

        // Act + Assert
        assertEquals(listOf(m), repo.obtenerTodas().first())
        assertEquals(m, repo.observarPorId("m-1").first())
        assertEquals(listOf(m), repo.obtenerHistoricoLinea("C", "T", "L").first())
        assertEquals(3, repo.contarPendientes().first())
        assertEquals(m, repo.obtenerPorId("m-1"))
        assertEquals(listOf(m), repo.obtenerPendientesSync())
        repo.marcarSincronizada("m-1")
        coVerify { dao.marcarSincronizada("m-1") }
    }
}
