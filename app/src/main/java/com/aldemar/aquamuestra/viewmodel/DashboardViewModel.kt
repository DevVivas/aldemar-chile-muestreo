package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Lo que la pantalla del dashboard necesita para dibujarse. */
data class DashboardUiState(
    val cargando: Boolean = true,
    val filtros: FiltrosDashboard = FiltrosDashboard(),
    /** Centros para el filtro (de todas las muestras). */
    val centros: List<String> = emptyList(),
    val indicadores: Indicadores = Indicadores(),
    /** Muestras que pasan los filtros (las usa el reporte PDF). */
    val muestrasFiltradas: List<MuestraEntity> = emptyList()
) {
    val vacio: Boolean get() = !cargando && indicadores.totalMuestras == 0
}

/**
 * ViewModel del dashboard. Junta tres fuentes y recalcula con [IndicadoresDashboard]:
 *  1. las muestras de Room (Flow: se actualiza solo),
 *  2. los filtros (período y centro),
 *  3. la línea elegida para el gráfico de evolución.
 */
class DashboardViewModel(
    repository: MuestraRepository,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val filtros = MutableStateFlow(FiltrosDashboard())
    private val lineaElegida = MutableStateFlow<LineaCultivo?>(null)

    val uiState: StateFlow<DashboardUiState> =
        combine(repository.obtenerTodas(), filtros, lineaElegida) { todas, f, linea ->
            val ahora = reloj()
            DashboardUiState(
                cargando = false,
                filtros = f,
                centros = IndicadoresDashboard.centros(todas),
                indicadores = IndicadoresDashboard.calcular(todas, f, ahora, linea),
                muestrasFiltradas = IndicadoresDashboard.filtrar(todas, f, ahora)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onPeriodoChange(periodo: PeriodoDashboard) = filtros.update { it.copy(periodo = periodo) }

    fun onCentroChange(centro: String?) = filtros.update { it.copy(centro = centro) }

    fun onLineaChange(linea: LineaCultivo) {
        lineaElegida.value = linea
    }

    /** Reporte PDF de las validadas que pasan los filtros actuales del dashboard. */
    fun prepararReporte(generadoPor: String): Reporte {
        val s = uiState.value
        return ReporteMuestras.preparar(
            muestras = s.muestrasFiltradas,
            filtros = ReporteMuestras.describir(s.filtros),
            generadoPor = generadoPor,
            fecha = reloj()
        )
    }
}
