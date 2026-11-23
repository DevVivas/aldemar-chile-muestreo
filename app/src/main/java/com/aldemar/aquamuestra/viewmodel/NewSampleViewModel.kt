package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

/**
 * ViewModel del formulario "Nueva muestra".
 * Guarda el estado de la pantalla, valida con [MuestraFormValidator] y guarda con [MuestraRepository].
 */
class NewSampleViewModel(
    private val repository: MuestraRepository,
    private val reloj: () -> Long = { System.currentTimeMillis() },
    generarId: () -> String = { UUID.randomUUID().toString() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NewSampleUiState(id = generarId(), fechaHora = reloj())
    )
    val uiState: StateFlow<NewSampleUiState> = _uiState.asStateFlow()

    // ---------------------------------------------------------------- Cambios de campos

    fun onCentroChange(valor: String) = editar { copy(centro = valor) }
    fun onConcesionChange(valor: String) = editar { copy(concesion = valor) }
    fun onTrenChange(valor: String) = editar { copy(tren = valor) }
    fun onLineaChange(valor: String) = editar { copy(linea = valor) }
    fun onOperadorChange(valor: String) = editar { copy(operador = valor) }
    fun onObservacionesChange(valor: String) = editar { copy(observaciones = valor) }

    /** Solo acepta dígitos (el teclado es numérico, pero se puede pegar texto). */
    fun onTramoCmChange(valor: String) = editar { copy(tramoCm = valor.filter(Char::isDigit).take(6)) }
    fun onCantidadChange(valor: String) = editar { copy(cantidad = valor.filter(Char::isDigit).take(6)) }

    /** Suma o resta al conteo con los botones ±. Nunca baja de 0. */
    fun onCantidadAjustar(delta: Int) = editar {
        val actual = cantidad.toIntOrNull() ?: 0
        copy(cantidad = (actual + delta).coerceAtLeast(0).toString())
    }

    /** Rellena el operador con el usuario de la sesión, solo si el campo está vacío. */
    fun precargarOperador(nombre: String?) {
        if (!nombre.isNullOrBlank() && _uiState.value.operador.isBlank()) onOperadorChange(nombre)
    }

    /**
     * Cambia el día manteniendo la hora.
     * @param fechaUtcMillis lo que entrega el DatePicker de Material 3 (medianoche en UTC).
     */
    fun onFechaChange(fechaUtcMillis: Long) = editar {
        val elegido = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = fechaUtcMillis }
        val nuevo = Calendar.getInstance().apply {
            timeInMillis = fechaHora
            set(elegido.get(Calendar.YEAR), elegido.get(Calendar.MONTH), elegido.get(Calendar.DAY_OF_MONTH))
        }
        copy(fechaHora = nuevo.timeInMillis)
    }

    /** Cambia la hora manteniendo el día. */
    fun onHoraChange(hora: Int, minuto: Int) = editar {
        val nuevo = Calendar.getInstance().apply {
            timeInMillis = fechaHora
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        copy(fechaHora = nuevo.timeInMillis)
    }

    fun onFotoCapturada(path: String) = editar { copy(fotoPath = path) }
    fun onFotoQuitada() = editar { copy(fotoPath = null) }

    // ---------------------------------------------------------------- Guardar

    fun guardarBorrador() = guardar(EstadoRevision.BORRADOR)

    fun enviar() = guardar(EstadoRevision.ENVIADA)

    /** La pantalla llama a esto cuando ya mostró el evento. */
    fun onEventoMostrado() = _uiState.update { it.copy(evento = null) }

    private fun guardar(estado: EstadoRevision) {
        val actual = _uiState.value
        if (actual.isSaving) return

        val errores = validar(actual, paraEnviar = estado == EstadoRevision.ENVIADA)
        if (errores.hayErrores) {
            _uiState.update {
                it.copy(errores = errores, intentoFallido = estado, evento = NewSampleEvento.HayErrores)
            }
            return
        }

        _uiState.update { it.copy(errores = ErroresFormulario(), intentoFallido = null, isSaving = true) }

        viewModelScope.launch {
            try {
                repository.guardar(actual.aEntidad(estado))
                _uiState.update { it.copy(isSaving = false, evento = NewSampleEvento.GuardadoOk(estado)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, evento = NewSampleEvento.ErrorAlGuardar) }
            }
        }
    }

    // ---------------------------------------------------------------- Ayudantes

    /** Aplica un cambio y, si ya hubo un intento fallido, vuelve a validar. */
    private fun editar(cambio: NewSampleUiState.() -> NewSampleUiState) {
        _uiState.update { estado ->
            val nuevo = estado.cambio()
            val intento = nuevo.intentoFallido
            if (intento == null) nuevo
            else nuevo.copy(errores = validar(nuevo, paraEnviar = intento == EstadoRevision.ENVIADA))
        }
    }

    private fun validar(s: NewSampleUiState, paraEnviar: Boolean) = MuestraFormValidator.validar(
        centro = s.centro,
        tren = s.tren,
        linea = s.linea,
        operador = s.operador,
        tramoCm = s.tramoCm,
        cantidad = s.cantidad,
        observaciones = s.observaciones,
        fotoPath = s.fotoPath,
        paraEnviar = paraEnviar
    )

    private fun NewSampleUiState.aEntidad(estado: EstadoRevision) = MuestraEntity(
        id = id,
        centro = centro.trim(),
        concesion = concesion.trim().ifBlank { null },
        tren = tren.trim(),
        linea = linea.trim(),
        tramoCm = tramoCm.trim().toInt(),
        fechaHora = fechaHora,
        operador = operador.trim(),
        cantidad = cantidad.trim().toInt(),
        observaciones = observaciones.trim(),
        estado = estado,
        fotoPath = fotoPath,
        pendienteSync = true,
        actualizadoEn = reloj()
    )
}
