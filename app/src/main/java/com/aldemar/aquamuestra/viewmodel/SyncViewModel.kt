package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.data.sync.EstadoSync
import com.aldemar.aquamuestra.data.sync.Sincronizador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Qué muestra el banner de sincronización. */
sealed interface BannerSync {
    /** Todo sincronizado: no se muestra nada. */
    data object Oculto : BannerSync

    /** Sin internet; [pendientes] muestras esperan para subir. */
    data class SinConexion(val pendientes: Int) : BannerSync

    /** WorkManager está sincronizando ahora. */
    data class Sincronizando(val pendientes: Int) : BannerSync

    /** Hay internet y [pendientes] muestras sin subir, pero no hay nada corriendo. */
    data class Pendientes(val pendientes: Int) : BannerSync
}

data class SyncUiState(
    val banner: BannerSync = BannerSync.Oculto,
    /** Para el indicador del pull-to-refresh. */
    val sincronizando: Boolean = false
)

/**
 * Junta tres fuentes y decide qué decir en el banner:
 *  - cuántas muestras están pendientes (Room),
 *  - si hay internet (ConectividadObserver),
 *  - si WorkManager está sincronizando (SyncScheduler).
 */
class SyncViewModel(
    repository: MuestraRepository,
    hayInternet: Flow<Boolean>,
    estadoSync: Flow<EstadoSync>,
    private val sincronizador: Sincronizador
) : ViewModel() {

    val uiState: StateFlow<SyncUiState> =
        combine(repository.contarPendientes(), hayInternet, estadoSync) { pendientes, internet, estado ->
            SyncUiState(
                banner = calcularBanner(pendientes, internet, estado),
                sincronizando = estado == EstadoSync.SINCRONIZANDO
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    fun sincronizarAhora() = sincronizador.sincronizarAhora()

    companion object {
        /** Regla del banner (función pura, fácil de probar). */
        fun calcularBanner(pendientes: Int, hayInternet: Boolean, estado: EstadoSync): BannerSync = when {
            !hayInternet -> BannerSync.SinConexion(pendientes)
            estado == EstadoSync.SINCRONIZANDO -> BannerSync.Sincronizando(pendientes)
            pendientes > 0 -> BannerSync.Pendientes(pendientes)
            else -> BannerSync.Oculto
        }
    }
}
