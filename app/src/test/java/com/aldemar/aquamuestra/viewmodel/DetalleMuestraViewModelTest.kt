package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.util.Fixtures.muestra
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleMuestraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<MuestraRepository>(relaxed = true)

    private fun TestScope.crear(m: MuestraEntity?, rol: UserRole?, nombre: String? = "Supervisor Demo"): DetalleMuestraViewModel {
        every { repo.observarPorId("m-1") } returns MutableStateFlow(m)
        every { repo.obtenerHistoricoLinea(any(), any(), any()) } returns flowOf(listOfNotNull(m))
        val vm = DetalleMuestraViewModel("m-1", repo, rol, nombre)
        // stateIn(WhileSubscribed) solo trabaja si alguien observa: este colector hace de "pantalla"
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun `dada una muestra enviada cuando la abre un supervisor entonces puede observar y validar y ve la densidad`() = runTest {
        // Act
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA, cantidad = 150, tramoCm = 100), UserRole.SUPERVISOR)

        // Assert
        val s = vm.uiState.value
        assertEquals(listOf(AccionRevision.OBSERVAR, AccionRevision.VALIDAR), s.acciones)
        assertEquals(150.0, s.densidad!!, 0.001)
        assertEquals(1, s.evolucion.size)
    }

    @Test
    fun `dada una muestra enviada cuando la abre un operario o un analista entonces no tiene acciones`() = runTest {
        assertTrue(crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.OPERARIO).uiState.value.acciones.isEmpty())
        assertTrue(crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.ANALISTA).uiState.value.acciones.isEmpty())
    }

    @Test
    fun `dado un supervisor cuando valida entonces llama al repositorio y avisa AccionOk`() = runTest {
        // Arrange
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.SUPERVISOR)

        // Act
        vm.validar()

        // Assert
        coVerify { repo.validar("m-1", "Supervisor Demo") }
        assertEquals(DetalleEvento.AccionOk(EstadoRevision.VALIDADA), vm.uiState.value.evento)

        // Act
        vm.onEventoMostrado()

        // Assert
        assertNull(vm.uiState.value.evento)
    }

    @Test
    fun `dado un operario cuando intenta validar entonces no pasa nada`() = runTest {
        // Arrange
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.OPERARIO)

        // Act
        vm.validar()

        // Assert
        coVerify(exactly = 0) { repo.validar(any(), any()) }
        assertNull(vm.uiState.value.evento)
    }

    @Test
    fun `dado un supervisor cuando observa sin comentario entonces da error y no guarda`() = runTest {
        // Arrange
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.SUPERVISOR)

        // Act
        vm.observar("   ")

        // Assert
        assertEquals(DetalleEvento.ComentarioVacio, vm.uiState.value.evento)
        coVerify(exactly = 0) { repo.observar(any(), any(), any()) }
    }

    @Test
    fun `dado un supervisor cuando observa con comentario entonces se guarda y avisa AccionOk`() = runTest {
        // Arrange
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.SUPERVISOR)

        // Act
        vm.observar("Repetir conteo")

        // Assert
        coVerify { repo.observar("m-1", "Supervisor Demo", "Repetir conteo") }
        assertEquals(DetalleEvento.AccionOk(EstadoRevision.OBSERVADA), vm.uiState.value.evento)
    }

    @Test
    fun `dado que Room falla cuando se valida entonces avisa error al guardar`() = runTest {
        // Arrange
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.SUPERVISOR)
        coEvery { repo.validar(any(), any()) } throws IllegalStateException("bd")

        // Act
        vm.validar()

        // Assert
        assertEquals(DetalleEvento.ErrorAlGuardar, vm.uiState.value.evento)
        assertEquals(false, vm.uiState.value.procesando)
    }

    @Test
    fun `dada una sesion sin nombre cuando se valida entonces no se llama al repositorio`() = runTest {
        // Arrange
        val vm = crear(muestra(estado = EstadoRevision.ENVIADA), UserRole.SUPERVISOR, nombre = null)

        // Act
        vm.validar()

        // Assert
        coVerify(exactly = 0) { repo.validar(any(), any()) }
    }

    @Test
    fun `dado un id que no existe cuando se abre entonces muestra no encontrada`() = runTest {
        // Act
        val vm = crear(null, UserRole.SUPERVISOR)

        // Assert
        assertTrue(vm.uiState.value.noEncontrada)
    }

    @Test
    fun `dado que Room falla al leer cuando se abre entonces muestra error`() = runTest {
        // Arrange
        every { repo.observarPorId("m-1") } returns flow { throw IllegalStateException("bd") }
        val vm = DetalleMuestraViewModel("m-1", repo, UserRole.SUPERVISOR, "Supervisor Demo")

        // Act
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        // Assert
        assertTrue(vm.uiState.value.error)
    }
}
