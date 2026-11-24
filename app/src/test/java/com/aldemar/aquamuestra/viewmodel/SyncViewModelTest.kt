package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.data.sync.EstadoSync
import com.aldemar.aquamuestra.data.sync.Sincronizador
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dado que no hay internet cuando se calcula el banner entonces dice sin conexion aunque este sincronizando`() {
        assertEquals(BannerSync.SinConexion(2), SyncViewModel.calcularBanner(2, hayInternet = false, EstadoSync.SINCRONIZANDO))
        assertEquals(BannerSync.SinConexion(0), SyncViewModel.calcularBanner(0, hayInternet = false, EstadoSync.INACTIVO))
    }

    @Test
    fun `dado internet y un trabajo corriendo cuando se calcula el banner entonces dice sincronizando`() {
        assertEquals(BannerSync.Sincronizando(3), SyncViewModel.calcularBanner(3, hayInternet = true, EstadoSync.SINCRONIZANDO))
    }

    @Test
    fun `dado internet y pendientes sin trabajo corriendo cuando se calcula el banner entonces dice pendientes`() {
        assertEquals(BannerSync.Pendientes(1), SyncViewModel.calcularBanner(1, hayInternet = true, EstadoSync.EN_COLA))
    }

    @Test
    fun `dado internet y nada pendiente cuando se calcula el banner entonces se oculta`() {
        assertEquals(BannerSync.Oculto, SyncViewModel.calcularBanner(0, hayInternet = true, EstadoSync.INACTIVO))
    }

    @Test
    fun `dadas las tres fuentes cuando cambian entonces el estado del banner se actualiza solo`() = runTest {
        // Arrange
        val repo = mockk<MuestraRepository>()
        val pendientes = MutableStateFlow(2)
        val internet = MutableStateFlow(false)
        val estado = MutableStateFlow(EstadoSync.EN_COLA)
        every { repo.contarPendientes() } returns pendientes
        val sincronizador = mockk<Sincronizador>(relaxed = true)
        val vm = SyncViewModel(repo, internet, estado, sincronizador)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        // Assert
        assertEquals(BannerSync.SinConexion(2), vm.uiState.value.banner)

        // Act: vuelve la red y arranca la sincronización
        internet.value = true
        estado.value = EstadoSync.SINCRONIZANDO

        // Assert
        assertEquals(BannerSync.Sincronizando(2), vm.uiState.value.banner)
        assertTrue(vm.uiState.value.sincronizando)

        // Act: termina
        pendientes.value = 0
        estado.value = EstadoSync.INACTIVO

        // Assert
        assertEquals(BannerSync.Oculto, vm.uiState.value.banner)
        assertFalse(vm.uiState.value.sincronizando)

        // Act: pull-to-refresh
        vm.sincronizarAhora()

        // Assert
        verify { sincronizador.sincronizarAhora() }
    }
}
