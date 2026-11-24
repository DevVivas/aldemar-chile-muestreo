package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Lo que el conteo devuelve al formulario. */
data class ResultadoConteo(
    val conteoMarcas: Int,
    val ajusteManual: Int,
    val marcasJson: String
)

/** Qué cambió con el último toque (para el feedback háptico/visual de la pantalla). */
enum class CambioConteo { AGREGADA, QUITADA, DESHECHA, LIMPIADO, AJUSTE }

data class ConteoUiState(
    val fotoPath: String? = null,
    val conteo: ConteoMarcas = ConteoMarcas(),
    /** Último cambio, para la vibración y la animación. Se reinicia cuando la pantalla lo usa. */
    val ultimoCambio: CambioConteo? = null
)

/**
 * ViewModel del conteo asistido. Solo guarda el estado y llama a [ConteoMarcas],
 * que tiene toda la lógica (agregar, quitar por cercanía, deshacer, total).
 */
class ConteoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConteoUiState())
    val uiState: StateFlow<ConteoUiState> = _uiState.asStateFlow()

    private var iniciado = false

    /** Carga la foto y el conteo que ya tenía la muestra (solo la primera vez; al rotar no se pierde). */
    fun iniciar(fotoPath: String?, marcasJson: String?, ajusteManual: Int) {
        if (iniciado) return
        iniciado = true
        _uiState.value = ConteoUiState(
            fotoPath = fotoPath,
            conteo = ConteoMarcas(ConteoMarcas.marcasDesdeJson(marcasJson), ajusteManual)
        )
    }

    /** Toque sobre la foto en (x, y) normalizados. Agrega una marca o quita la que esté debajo. */
    fun tocar(x: Float, y: Float, toleranciaX: Float, toleranciaY: Float) = _uiState.update {
        val (nuevo, agregada) = it.conteo.tocar(x, y, toleranciaX, toleranciaY)
        if (nuevo == it.conteo) it
        else it.copy(conteo = nuevo, ultimoCambio = if (agregada) CambioConteo.AGREGADA else CambioConteo.QUITADA)
    }

    fun deshacer() = _uiState.update {
        if (it.conteo.marcas.isEmpty()) it else it.copy(conteo = it.conteo.deshacer(), ultimoCambio = CambioConteo.DESHECHA)
    }

    fun limpiar() = _uiState.update { it.copy(conteo = it.conteo.limpiar(), ultimoCambio = CambioConteo.LIMPIADO) }

    fun ajustar(delta: Int) = _uiState.update {
        val nuevo = it.conteo.ajustar(delta)
        if (nuevo == it.conteo) it else it.copy(conteo = nuevo, ultimoCambio = CambioConteo.AJUSTE)
    }

    fun onCambioMostrado() = _uiState.update { it.copy(ultimoCambio = null) }

    fun resultado(): ResultadoConteo = _uiState.value.conteo.let {
        ResultadoConteo(conteoMarcas = it.conteoMarcas, ajusteManual = it.ajusteManual, marcasJson = it.aJson())
    }
}
