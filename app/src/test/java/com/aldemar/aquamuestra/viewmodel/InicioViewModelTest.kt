package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.data.sync.ResultadoSync
import com.aldemar.aquamuestra.util.Fixtures.muestra
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
class InicioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<MuestraRepository>()
    private val internet = MutableStateFlow(true)
    private var resultadoSync: suspend () -> ResultadoSync = { ResultadoSync.COMPLETO }
    private var vecesSincronizado = 0

    private val tabla = listOf(
        muestra(id = "a", operador = "Operario Demo", estado = EstadoRevision.ENVIADA, fechaHora = 1L),
        muestra(id = "b", operador = "Operario Norte", estado = EstadoRevision.ENVIADA, fechaHora = 2L),
        muestra(id = "c", operador = "Operario Demo", estado = EstadoRevision.BORRADOR, fechaHora = 3L),
        muestra(id = "d", operador = "Operario Demo", estado = EstadoRevision.VALIDADA, fechaHora = 4L),
        muestra(id = "e", operador = "Operario Demo", estado = EstadoRevision.OBSERVADA, fechaHora = 5L)
    )

    private fun TestScope.crear(rol: UserRole, nombre: String = "Operario Demo"): InicioViewModel {
        every { repo.obtenerTodas() } returns MutableStateFlow(tabla)
        val vm = InicioViewModel(repo, rol, nombre, internet, sincronizar = {
            vecesSincronizado++
            resultadoSync()
        })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun `dado un operario cuando abre inicio entonces ve sus 3 muestras mas recientes`() = runTest {
        // Act
        val s = crear(UserRole.OPERARIO).uiState.value

        // Assert: "b" es de otro operario y no aparece
        assertFalse(s.cargando)
        assertEquals(listOf("e", "d", "c"), s.ultimas.map { it.id })
    }

    @Test
    fun `dado un supervisor cuando abre inicio entonces ve las ultimas de todos y cuantas enviadas hay por revisar`() = runTest {
        // Act
        val s = crear(UserRole.SUPERVISOR, "Supervisor Demo").uiState.value

        // Assert
        assertEquals(listOf("e", "d", "c"), s.ultimas.map { it.id })
        assertEquals(2, s.porRevisar)
    }

    @Test
    fun `dado que no hay pendientes cuando cierra sesion entonces sale directo sin preguntar`() = runTest {
        // Arrange
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returns emptyList()

        // Act
        vm.solicitarCierre()

        // Assert
        assertEquals(CierreSesion.Salir, vm.uiState.value.cierre)

        // Act: la pantalla ya navegó al login
        vm.onSalidaHecha()

        // Assert
        assertEquals(CierreSesion.Inactivo, vm.uiState.value.cierre)
    }

    @Test
    fun `dadas 2 muestras pendientes cuando cierra sesion entonces pregunta mostrando cuantas son`() = runTest {
        // Arrange
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returns tabla.take(2)

        // Act
        vm.solicitarCierre()

        // Assert
        assertEquals(CierreSesion.Confirmar(pendientes = 2), vm.uiState.value.cierre)
    }

    @Test
    fun `dadas pendientes cuando elige sincronizar y salir y todo sube entonces sale`() = runTest {
        // Arrange: antes de sincronizar hay 2; después, 0
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returnsMany listOf(tabla.take(2), emptyList())
        vm.solicitarCierre()

        // Act
        vm.sincronizarYSalir()

        // Assert
        assertEquals(1, vecesSincronizado)
        assertEquals(CierreSesion.Salir, vm.uiState.value.cierre)
    }

    @Test
    fun `dado que no hay internet cuando elige sincronizar y salir entonces avisa y no sale`() = runTest {
        // Arrange
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returns tabla.take(2)
        internet.value = false
        vm.solicitarCierre()

        // Act
        vm.sincronizarYSalir()

        // Assert
        assertEquals(CierreSesion.Confirmar(2, aviso = AvisoCierre.SIN_CONEXION), vm.uiState.value.cierre)
        assertEquals(0, vecesSincronizado)
    }

    @Test
    fun `dado que el servidor no acepta todo cuando sincroniza entonces avisa cuantas quedan`() = runTest {
        // Arrange: quedan 1 pendiente después; además la sincronización lanza un error
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returnsMany listOf(tabla.take(2), tabla.take(1))
        resultadoSync = { throw IllegalStateException("servidor caído") }
        vm.solicitarCierre()

        // Act
        vm.sincronizarYSalir()

        // Assert
        assertEquals(CierreSesion.Confirmar(1, aviso = AvisoCierre.QUEDAN_PENDIENTES), vm.uiState.value.cierre)
    }

    @Test
    fun `dado que esta sincronizando cuando se vuelve a tocar entonces no sincroniza dos veces`() = runTest {
        // Arrange: la sincronización tarda (no termina hasta completar el diferido)
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returnsMany listOf(tabla.take(2), emptyList())
        val fin = CompletableDeferred<ResultadoSync>()
        resultadoSync = { fin.await() }
        vm.solicitarCierre()

        // Act
        vm.sincronizarYSalir()
        val mientras = vm.uiState.value.cierre as CierreSesion.Confirmar
        vm.sincronizarYSalir()
        fin.complete(ResultadoSync.COMPLETO)

        // Assert
        assertTrue(mientras.sincronizando)
        assertEquals(1, vecesSincronizado)
        assertEquals(CierreSesion.Salir, vm.uiState.value.cierre)
    }

    @Test
    fun `dadas pendientes cuando elige salir igual o cancelar entonces sale o vuelve a inicio`() = runTest {
        // Arrange
        val vm = crear(UserRole.OPERARIO)
        coEvery { repo.obtenerPendientesSync() } returns tabla.take(2)
        vm.sincronizarYSalir() // sin diálogo abierto no hace nada
        assertEquals(CierreSesion.Inactivo, vm.uiState.value.cierre)

        // Act + Assert: cancelar
        vm.solicitarCierre()
        vm.cancelarCierre()
        assertEquals(CierreSesion.Inactivo, vm.uiState.value.cierre)

        // Act + Assert: salir igual (las muestras quedan guardadas en el teléfono)
        vm.solicitarCierre()
        vm.salirIgual()
        assertEquals(CierreSesion.Salir, vm.uiState.value.cierre)
        assertEquals(0, vecesSincronizado)
    }
}
