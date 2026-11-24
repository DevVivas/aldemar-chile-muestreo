package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.util.Fixtures.muestra
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<MuestraRepository>()
    private val tabla = MutableStateFlow(
        listOf(
            muestra(id = "a", centro = "Calbuco Sur", estado = EstadoRevision.ENVIADA, fechaHora = 3_000L),
            muestra(id = "b", centro = "Reloncaví Estuario", estado = EstadoRevision.VALIDADA, fechaHora = 2_000L),
            muestra(id = "c", centro = "Calbuco Sur", estado = EstadoRevision.BORRADOR, fechaHora = 1_000L)
        )
    )

    @Test
    fun `dado que nadie observa cuando se crea entonces el estado inicial esta cargando`() {
        // Arrange
        every { repo.obtenerTodas() } returns tabla

        // Act + Assert
        assertTrue(HistorialViewModel(repo).uiState.value.cargando)
    }

    @Test
    fun `dadas muestras en Room cuando se filtra por centro y estado entonces la lista se recalcula sola`() = runTest {
        // Arrange
        every { repo.obtenerTodas() } returns tabla
        val vm = HistorialViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        // Assert: sin filtros, todas y sus opciones
        assertFalse(vm.uiState.value.cargando)
        assertEquals(3, vm.uiState.value.total)
        assertEquals(listOf("a", "b", "c"), vm.uiState.value.muestras.map { it.id })

        // Act
        vm.onCentroChange("Calbuco Sur")
        vm.onEstadoChange(EstadoRevision.BORRADOR)

        // Assert
        assertEquals(listOf("c"), vm.uiState.value.muestras.map { it.id })
        assertEquals(3, vm.uiState.value.total)

        // Act: llega una muestra nueva a Room (el DAO entrega de la más nueva a la más antigua)
        tabla.value = listOf(muestra(id = "d", centro = "Calbuco Sur", estado = EstadoRevision.BORRADOR, fechaHora = 4_000L)) + tabla.value

        // Assert
        assertEquals(listOf("d", "c"), vm.uiState.value.muestras.map { it.id })
    }

    @Test
    fun `dados los filtros del historial cuando se prepara el reporte entonces trae solo las validadas visibles`() = runTest {
        // Arrange
        every { repo.obtenerTodas() } returns tabla
        val vm = HistorialViewModel(repo, reloj = { 0L })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        vm.onCentroChange("Reloncaví Estuario")

        // Act
        val r = vm.prepararReporte("Analista Demo (Analista de Datos)")

        // Assert
        assertEquals(listOf("b"), r.filas.map { it.id })
        assertEquals(listOf("Centro: Reloncaví Estuario"), r.filtros)
    }

    @Test
    fun `dados varios filtros cuando se limpian entonces vuelven a verse todas`() = runTest {
        // Arrange
        every { repo.obtenerTodas() } returns tabla
        val vm = HistorialViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        vm.onTrenChange("Tren 9")
        vm.onLineaChange("Línea 99")
        vm.onRangoFechasChange(0L, 0L)
        assertTrue(vm.uiState.value.muestras.isEmpty())

        // Act
        vm.limpiarFiltros()

        // Assert
        assertEquals(FiltrosHistorial(), vm.uiState.value.filtros)
        assertEquals(3, vm.uiState.value.muestras.size)
    }
}
