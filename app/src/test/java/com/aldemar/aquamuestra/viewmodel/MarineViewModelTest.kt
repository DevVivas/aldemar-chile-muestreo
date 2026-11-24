package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.CentrosCatalogo
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.marine.CondicionesMarinas
import com.aldemar.aquamuestra.data.remote.marine.MarineRepository
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MarineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<MarineRepository>()
    private val calbuco = CentrosCatalogo.centros[0]
    private val huito = CentrosCatalogo.centros[1]
    private val dato = CondicionesMarinas(0.22, 251.0, 2.65, 15.8, 0.4, 270.0, "2026-11-24T01:45", 1L)
    private val viejo = dato.copy(alturaOlaM = 0.5, consultadoEn = 0L)

    @Test
    fun `dada una respuesta ok cuando se abre entonces consulta el primer centro y muestra el dato`() = runTest {
        // Arrange
        coEvery { repo.consultar(calbuco) } returns Resultado.Ok(dato)

        // Act
        val vm = MarineViewModel(repo)

        // Assert
        assertEquals(calbuco, vm.uiState.value.centro)
        assertEquals(EstadoMarino.Ok(dato), vm.uiState.value.estado)
    }

    @Test
    fun `dado que no hay internet y hay cache cuando se consulta entonces muestra el ultimo dato guardado`() = runTest {
        // Arrange
        coEvery { repo.consultar(any()) } returns Resultado.SinConexion
        coEvery { repo.leerCache(calbuco) } returns viejo

        // Act
        val vm = MarineViewModel(repo)

        // Assert
        assertEquals(EstadoMarino.SinConexion(viejo), vm.uiState.value.estado)
    }

    @Test
    fun `dado que no hay internet ni cache cuando se consulta entonces muestra sin conexion vacio`() = runTest {
        // Arrange
        coEvery { repo.consultar(any()) } returns Resultado.SinConexion
        coEvery { repo.leerCache(any()) } returns null

        // Act
        val vm = MarineViewModel(repo)

        // Assert
        assertEquals(EstadoMarino.SinConexion(null), vm.uiState.value.estado)
    }

    @Test
    fun `dado un error de Open-Meteo cuando se consulta entonces muestra el codigo y el cache`() = runTest {
        // Arrange
        coEvery { repo.consultar(any()) } returns Resultado.ErrorHttp(400, "mal")
        coEvery { repo.leerCache(any()) } returns viejo

        // Act
        val vm = MarineViewModel(repo)

        // Assert
        assertEquals(EstadoMarino.Error(400, viejo), vm.uiState.value.estado)
    }

    @Test
    fun `dado otro centro cuando se recarga entonces queda cargando y luego muestra su dato`() = runTest {
        // Arrange
        coEvery { repo.consultar(calbuco) } returns Resultado.Ok(dato)
        val respuestaLenta = CompletableDeferred<Resultado<CondicionesMarinas>>()
        coEvery { repo.consultar(huito) } coAnswers { respuestaLenta.await() }
        val vm = MarineViewModel(repo)

        // Act
        vm.recargar(huito)

        // Assert: mientras responde
        assertEquals(huito, vm.uiState.value.centro)
        assertEquals(EstadoMarino.Cargando, vm.uiState.value.estado)

        // Act: llega la respuesta
        respuestaLenta.complete(Resultado.Ok(viejo))

        // Assert
        assertEquals(EstadoMarino.Ok(viejo), vm.uiState.value.estado)
        coVerify { repo.consultar(huito) }
    }
}
