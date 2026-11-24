package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.data.local.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todo lo que necesita la pantalla de detalle. */
data class DetalleUiState(
    val cargando: Boolean = true,
    val noEncontrada: Boolean = false,
    val error: Boolean = false,
    val muestra: MuestraEntity? = null,
    val densidad: Double? = null,
    /** Muestreos de la misma línea, del más antiguo al más nuevo. */
    val evolucion: List<PuntoEvolucion> = emptyList(),
    val rol: UserRole? = null,
    val acciones: List<AccionRevision> = emptyList(),
    /** true mientras se guarda una acción (los botones se deshabilitan). */
    val procesando: Boolean = false,
    val evento: DetalleEvento? = null
)

sealed interface DetalleEvento {
    data class AccionOk(val nuevoEstado: EstadoRevision) : DetalleEvento
    data object ComentarioVacio : DetalleEvento
    data object ErrorAlGuardar : DetalleEvento
}

/**
 * ViewModel del detalle de una muestra.
 *
 * @param rol y [nombreUsuario] vienen de la sesión; se reciben por constructor para poder probarlo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleMuestraViewModel(
    private val muestraId: String,
    private val repository: MuestraRepository,
    private val rol: UserRole?,
    private val nombreUsuario: String?
) : ViewModel() {

    /** Lo que cambia por acciones del usuario (no viene de Room). */
    private data class EstadoAccion(val procesando: Boolean = false, val evento: DetalleEvento? = null)

    private val estadoAccion = MutableStateFlow(EstadoAccion())

    /** Muestra + histórico de su línea, ambos desde Room (se actualizan solos). */
    private val datos: Flow<DetalleUiState> =
        repository.observarPorId(muestraId)
            .flatMapLatest { muestra ->
                if (muestra == null) {
                    flowOf(DetalleUiState(cargando = false, noEncontrada = true, rol = rol))
                } else {
                    repository.obtenerHistoricoLinea(muestra.centro, muestra.tren, muestra.linea)
                        .map { historico ->
                            DetalleUiState(
                                cargando = false,
                                muestra = muestra,
                                densidad = densidadPorMetro(muestra.cantidad, muestra.tramoCm),
                                evolucion = EvolucionLinea.calcular(historico, muestra.id),
                                rol = rol,
                                acciones = AccionesRevision.disponibles(rol, muestra.estado, muestra.operador, nombreUsuario)
                            )
                        }
                }
            }
            .catch { emit(DetalleUiState(cargando = false, error = true, rol = rol)) }

    val uiState: StateFlow<DetalleUiState> =
        combine(datos, estadoAccion) { d, a -> d.copy(procesando = a.procesando, evento = a.evento) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState(rol = rol))

    fun validar() = ejecutar(AccionRevision.VALIDAR, EstadoRevision.VALIDADA) { supervisor ->
        repository.validar(muestraId, supervisor)
    }

    fun observar(comentario: String) {
        if (!AccionesRevision.comentarioValido(comentario)) {
            estadoAccion.update { it.copy(evento = DetalleEvento.ComentarioVacio) }
            return
        }
        ejecutar(AccionRevision.OBSERVAR, EstadoRevision.OBSERVADA) { supervisor ->
            repository.observar(muestraId, supervisor, comentario)
        }
    }

    fun onEventoMostrado() = estadoAccion.update { it.copy(evento = null) }

    /** Revisa que la acción esté permitida para este rol y estado, y la ejecuta. */
    private fun ejecutar(accion: AccionRevision, nuevoEstado: EstadoRevision, bloque: suspend (String) -> Unit) {
        val actual = uiState.value
        if (actual.procesando || accion !in actual.acciones) return
        val supervisor = nombreUsuario ?: return

        estadoAccion.update { it.copy(procesando = true) }
        viewModelScope.launch {
            try {
                bloque(supervisor)
                estadoAccion.update { EstadoAccion(evento = DetalleEvento.AccionOk(nuevoEstado)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                estadoAccion.update { EstadoAccion(evento = DetalleEvento.ErrorAlGuardar) }
            }
        }
    }
}
