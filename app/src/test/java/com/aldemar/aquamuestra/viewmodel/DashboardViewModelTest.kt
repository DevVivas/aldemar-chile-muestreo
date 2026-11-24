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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dia = 24L * 60 * 60 * 1000
    private val ahora = 100 * dia
    private val repo = mockk<MuestraRepository>()
    private val tabla = MutableStateFlow(
        listOf(
            muestra(id = "a", centro = "Calbuco Sur", linea = "L1", estado = EstadoRevision.VALIDADA, fechaHora = ahora - dia, cantidad = 100),
            muestra(id = "b", centro = "Reloncaví Estuario", linea = "L2", estado = EstadoRevision.ENVIADA, fechaHora = ahora - 3 * dia, cantidad = 50),
            muestra(id = "c", centro = "Calbuco Sur", linea = "L3", estado = EstadoRevision.VALIDADA, fechaHora = ahora - 60 * dia, cantidad = 70)
        )
    )

    private fun TestScope.crear(): DashboardViewModel {
        every { repo.obtenerTodas() } returns tabla
        val vm = DashboardViewModel(repo, reloj = { ahora })
        // stateIn(WhileSubscribed): este colector hace de "pantalla abierta"
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun `dado que nadie observa cuando se crea entonces el estado inicial esta cargando`() {
        every { repo.obtenerTodas() } returns tabla
        assertTrue(DashboardViewModel(repo).uiState.value.cargando)
    }

    @Test
    fun `dadas muestras en Room cuando se abre entonces calcula con 30 dias por defecto`() = runTest {
        // Act
        val s = crear().uiState.value

        // Assert
        assertFalse(s.cargando)
        assertEquals(PeriodoDashboard.TREINTA_DIAS, s.filtros.periodo)
        assertEquals(2, s.indicadores.totalMuestras) // "c" tiene 60 días
        assertEquals(listOf("Calbuco Sur", "Reloncaví Estuario"), s.centros)
        assertEquals(listOf("a", "b"), s.muestrasFiltradas.map { it.id })
    }

    @Test
    fun `dado un cambio de periodo y centro cuando se aplica entonces los indicadores se recalculan solos`() = runTest {
        // Arrange
        val vm = crear()

        // Act
        vm.onPeriodoChange(PeriodoDashboard.TODO)
        vm.onCentroChange("Calbuco Sur")

        // Assert
        assertEquals(2, vm.uiState.value.indicadores.totalMuestras)
        assertEquals(170, vm.uiState.value.indicadores.totalIndividuos)

        // Act: llega una muestra nueva a Room
        tabla.value = tabla.value + muestra(id = "d", centro = "Calbuco Sur", fechaHora = ahora, cantidad = 30)

        // Assert
        assertEquals(3, vm.uiState.value.indicadores.totalMuestras)
    }

    @Test
    fun `dados filtros sin muestras cuando se aplican entonces el estado es vacio`() = runTest {
        // Arrange
        val vm = crear()

        // Act
        vm.onPeriodoChange(PeriodoDashboard.SIETE_DIAS)
        vm.onCentroChange("Centro sin muestras")

        // Assert
        assertTrue(vm.uiState.value.vacio)
    }

    @Test
    fun `dada otra linea elegida cuando se cambia entonces el grafico de evolucion usa esa linea`() = runTest {
        // Arrange
        val vm = crear()
        val l2 = LineaCultivo("Reloncaví Estuario", "Tren 1 - Módulo A", "L2")

        // Act
        vm.onLineaChange(l2)

        // Assert
        assertEquals(l2, vm.uiState.value.indicadores.lineaActiva)
        assertEquals(listOf(50), vm.uiState.value.indicadores.evolucion.map { it.cantidad })
    }

    @Test
    fun `dados los filtros del dashboard cuando se prepara el reporte entonces solo trae las validadas de esos filtros`() = runTest {
        // Arrange
        val vm = crear()

        // Act
        val r = vm.prepararReporte("Supervisor Demo (Supervisor de Calidad)")

        // Assert: en 30 días hay "a" (validada) y "b" (enviada); "c" queda fuera por fecha
        assertEquals(listOf("a"), r.filas.map { it.id })
        assertEquals(listOf("Período: últimos 30 días", "Centro: todos"), r.filtros)
        assertEquals("Supervisor Demo (Supervisor de Calidad)", r.generadoPor)
    }
}
