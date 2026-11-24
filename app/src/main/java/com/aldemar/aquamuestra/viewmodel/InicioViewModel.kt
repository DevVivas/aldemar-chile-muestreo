package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.data.sync.ResultadoSync
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Qué pasa con el cierre de sesión. */
sealed interface CierreSesion {
    data object Inactivo : CierreSesion

    /** Hay muestras sin subir: el diálogo pregunta qué hacer. */
    data class Confirmar(
        val pendientes: Int,
        val sincronizando: Boolean = false,
        val aviso: AvisoCierre? = null
    ) : CierreSesion

    /** Ya se puede cerrar la sesión (la pantalla navega al login). */
    data object Salir : CierreSesion
}

enum class AvisoCierre {
    /** No hay internet: no se puede sincronizar ahora. */
    SIN_CONEXION,

    /** Se intentó sincronizar, pero el servidor no aceptó todo (quedan pendientes). */
    QUEDAN_PENDIENTES
}

data class InicioUiState(
    val cargando: Boolean = true,
    /** Últimas 3 muestras: las propias si es operario; todas si es supervisor o analista. */
    val ultimas: List<MuestraEntity> = emptyList(),
    /** Muestras ENVIADAS esperando revisión (tarjeta "Por revisar" del supervisor). */
    val porRevisar: Int = 0,
    val cierre: CierreSesion = CierreSesion.Inactivo
)

/**
 * ViewModel de Inicio.
 *
 * @param hayInternet si hay conexión ahora (ConectividadObserver).
 * @param sincronizar sube lo pendiente y espera el resultado (en la app: SincronizadorMuestras).
 */
class InicioViewModel(
    private val repository: MuestraRepository,
    private val rol: UserRole?,
    private val nombreUsuario: String?,
    private val hayInternet: Flow<Boolean>,
    private val sincronizar: suspend () -> ResultadoSync
) : ViewModel() {

    private val cierre = MutableStateFlow<CierreSesion>(CierreSesion.Inactivo)

    val uiState: StateFlow<InicioUiState> =
        combine(repository.obtenerTodas(), cierre) { todas, c ->
            val visibles = if (rol == UserRole.OPERARIO) {
                todas.filter { AccionesRevision.esDelUsuario(it.operador, nombreUsuario) }
            } else {
                todas
            }
            InicioUiState(
                cargando = false,
                ultimas = visibles.sortedByDescending { it.fechaHora }.take(ULTIMAS),
                porRevisar = todas.count { it.estado == EstadoRevision.ENVIADA },
                cierre = c
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState())

    /** "Cerrar sesión": si no hay nada pendiente sale directo; si hay, pregunta. */
    fun solicitarCierre() {
        viewModelScope.launch {
            val pendientes = repository.obtenerPendientesSync().size
            cierre.value = if (pendientes == 0) CierreSesion.Salir else CierreSesion.Confirmar(pendientes)
        }
    }

    /** "Sincronizar y salir": espera a que termine; si no hay red o quedan pendientes, avisa y no sale. */
    fun sincronizarYSalir() {
        val actual = cierre.value as? CierreSesion.Confirmar ?: return
        if (actual.sincronizando) return
        cierre.value = actual.copy(sincronizando = true, aviso = null)
        viewModelScope.launch {
            if (!hayInternet.first()) {
                cierre.value = actual.copy(sincronizando = false, aviso = AvisoCierre.SIN_CONEXION)
                return@launch
            }
            try {
                sincronizar()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Lo que no se subió sigue pendiente: se informa abajo con el conteo real.
            }
            val quedan = repository.obtenerPendientesSync().size
            cierre.value = if (quedan == 0) {
                CierreSesion.Salir
            } else {
                CierreSesion.Confirmar(quedan, aviso = AvisoCierre.QUEDAN_PENDIENTES)
            }
        }
    }

    /** "Salir igual": las muestras quedan guardadas en el teléfono y se subirán en el próximo inicio. */
    fun salirIgual() {
        cierre.value = CierreSesion.Salir
    }

    fun cancelarCierre() {
        cierre.value = CierreSesion.Inactivo
    }

    /** La pantalla ya cerró la sesión y navegó: se vuelve al estado inicial. */
    fun onSalidaHecha() {
        cierre.value = CierreSesion.Inactivo
    }

    companion object {
        const val ULTIMAS = 3
    }
}
