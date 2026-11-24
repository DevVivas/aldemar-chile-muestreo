package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Lo que la pantalla de historial necesita para dibujarse. */
data class HistorialUiState(
    val cargando: Boolean = true,
    /** Muestras que pasan los filtros, de la más reciente a la más antigua. */
    val muestras: List<MuestraEntity> = emptyList(),
    /** Cuántas muestras hay guardadas en total (sin filtrar). */
    val total: Int = 0,
    val filtros: FiltrosHistorial = FiltrosHistorial(),
    val opciones: OpcionesFiltro = OpcionesFiltro()
)

/**
 * ViewModel del historial. Junta dos fuentes:
 *  1. la lista de Room (un Flow que se actualiza solo cuando cambia la tabla), y
 *  2. los filtros que elige el usuario.
 * Cada vez que cambia cualquiera de las dos, recalcula la lista con [FiltroMuestras.aplicar].
 */
class HistorialViewModel(
    repository: MuestraRepository,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val filtros = MutableStateFlow(FiltrosHistorial())

    val uiState: StateFlow<HistorialUiState> =
        combine(repository.obtenerTodas(), filtros) { todas, f ->
            HistorialUiState(
                cargando = false,
                muestras = FiltroMuestras.aplicar(todas, f),
                total = todas.size,
                filtros = f,
                opciones = FiltroMuestras.opciones(todas)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistorialUiState(cargando = true)
        )

    fun onCentroChange(valor: String?) = filtros.update { it.copy(centro = valor) }
    fun onTrenChange(valor: String?) = filtros.update { it.copy(tren = valor) }
    fun onLineaChange(valor: String?) = filtros.update { it.copy(linea = valor) }
    fun onEstadoChange(valor: EstadoRevision?) = filtros.update { it.copy(estado = valor) }

    /** Fechas en "medianoche UTC" (lo que entrega el DateRangePicker). null = sin límite. */
    fun onRangoFechasChange(desdeUtc: Long?, hastaUtc: Long?) =
        filtros.update { it.copy(desdeUtc = desdeUtc, hastaUtc = hastaUtc) }

    fun limpiarFiltros() = filtros.update { FiltrosHistorial() }

    /** Reporte PDF de las validadas que se ven ahora en el historial (con sus filtros). */
    fun prepararReporte(generadoPor: String): Reporte {
        val s = uiState.value
        return ReporteMuestras.preparar(
            muestras = s.muestras,
            filtros = ReporteMuestras.describir(s.filtros),
            generadoPor = generadoPor,
            fecha = reloj()
        )
    }
}
