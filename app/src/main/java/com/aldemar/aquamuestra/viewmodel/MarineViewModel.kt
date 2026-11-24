package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.CentroUbicacion
import com.aldemar.aquamuestra.data.local.CentrosCatalogo
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.marine.CondicionesMarinas
import com.aldemar.aquamuestra.data.remote.marine.MarineRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Qué muestra la tarjeta de condiciones marinas. */
sealed interface EstadoMarino {
    data object Cargando : EstadoMarino

    /** Dato recién traído de Open-Meteo. */
    data class Ok(val datos: CondicionesMarinas) : EstadoMarino

    /** Sin internet: se muestra el último dato guardado (si hay). */
    data class SinConexion(val cache: CondicionesMarinas?) : EstadoMarino

    /** Open-Meteo respondió con error: botón "Reintentar" y, si hay, el último dato guardado. */
    data class Error(val codigo: Int, val cache: CondicionesMarinas?) : EstadoMarino
}

data class MarineUiState(
    val centros: List<CentroUbicacion> = CentrosCatalogo.centros,
    val centro: CentroUbicacion = CentrosCatalogo.centros.first(),
    val estado: EstadoMarino = EstadoMarino.Cargando
)

/** ViewModel de la tarjeta "Condiciones marinas" (API externa Open-Meteo). */
class MarineViewModel(private val repository: MarineRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MarineUiState())
    val uiState: StateFlow<MarineUiState> = _uiState.asStateFlow()

    private var consulta: Job? = null

    init {
        recargar(_uiState.value.centro)
    }

    /** Consulta (o vuelve a consultar) el centro. También sirve para cambiar de centro. */
    fun recargar(centro: CentroUbicacion = _uiState.value.centro) {
        consulta?.cancel() // si se cambia rápido de centro, solo vale la última consulta
        _uiState.update { it.copy(centro = centro, estado = EstadoMarino.Cargando) }
        consulta = viewModelScope.launch {
            val estado = when (val r = repository.consultar(centro)) {
                is Resultado.Ok -> EstadoMarino.Ok(r.datos)
                Resultado.SinConexion -> EstadoMarino.SinConexion(repository.leerCache(centro))
                is Resultado.ErrorHttp -> EstadoMarino.Error(r.codigo, repository.leerCache(centro))
            }
            _uiState.update { it.copy(estado = estado) }
        }
    }
}
