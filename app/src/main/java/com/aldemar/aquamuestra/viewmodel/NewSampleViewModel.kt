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
    /** Si viene, el formulario abre esa muestra para editarla/corregirla (mismo ID). */
    muestraIdEditar: String? = null,
    private val reloj: () -> Long = { System.currentTimeMillis() },
    generarId: () -> String = { UUID.randomUUID().toString() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        if (muestraIdEditar == null) {
            NewSampleUiState(id = generarId(), fechaHora = reloj())
        } else {
            NewSampleUiState(id = muestraIdEditar, cargando = true)
        }
    )
    val uiState: StateFlow<NewSampleUiState> = _uiState.asStateFlow()

    /** La muestra tal como estaba en Room al abrir la edición (para conservar sus datos de revisión). */
    private var original: MuestraEntity? = null

    init {
        if (muestraIdEditar != null) cargarParaEditar(muestraIdEditar)
    }

    private fun cargarParaEditar(id: String) {
        viewModelScope.launch {
            val m = repository.obtenerPorId(id)
            original = m
            _uiState.update { s ->
                if (m == null) {
                    s.copy(cargando = false)
                } else {
                    s.copy(
                        cargando = false,
                        estadoOriginal = m.estado,
                        centro = m.centro,
                        concesion = m.concesion.orEmpty(),
                        tren = m.tren,
                        linea = m.linea,
                        operador = m.operador,
                        tramoCm = m.tramoCm.toString(),
                        conteoMarcas = m.conteoMarcas,
                        ajusteManual = m.ajusteManual,
                        marcasJson = m.marcasJson,
                        observaciones = m.observaciones,
                        fechaHora = m.fechaHora,
                        fotoPath = m.fotoPath
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------- Cambios de campos

    fun onCentroChange(valor: String) = editar { copy(centro = valor) }
    fun onConcesionChange(valor: String) = editar { copy(concesion = valor) }
    fun onTrenChange(valor: String) = editar { copy(tren = valor) }
    fun onLineaChange(valor: String) = editar { copy(linea = valor) }
    fun onOperadorChange(valor: String) = editar { copy(operador = valor) }
    fun onObservacionesChange(valor: String) = editar { copy(observaciones = valor) }

    /** Solo acepta dígitos (el teclado es numérico, pero se puede pegar texto). */
    fun onTramoCmChange(valor: String) = editar { copy(tramoCm = valor.filter(Char::isDigit).take(6)) }
    /** Resultado de la pantalla de conteo. */
    fun onConteoGuardado(conteoMarcas: Int, ajusteManual: Int, marcasJson: String) = editar {
        copy(conteoMarcas = conteoMarcas, ajusteManual = ajusteManual, marcasJson = marcasJson)
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

    /**
     * Foto nueva desde la cámara. Las marcas anteriores apuntaban a otra imagen, así que se borran
     * (el ajuste manual se conserva).
     */
    fun onFotoCapturada(path: String) = editar {
        copy(fotoPath = path, conteoMarcas = 0, marcasJson = null, ajusteManual = ajusteManual.coerceAtLeast(0))
    }
    fun onFotoQuitada() = editar {
        copy(fotoPath = null, conteoMarcas = 0, marcasJson = null, ajusteManual = ajusteManual.coerceAtLeast(0))
    }

    // ---------------------------------------------------------------- Guardar

    fun guardarBorrador() = guardar(EstadoRevision.BORRADOR)

    fun enviar() = guardar(EstadoRevision.ENVIADA)

    /** La pantalla llama a esto cuando ya mostró el evento. */
    fun onEventoMostrado() = _uiState.update { it.copy(evento = null) }

    private fun guardar(estado: EstadoRevision) {
        val actual = _uiState.value
        if (actual.isSaving || actual.cargando) return

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
        cantidad = s.cantidad.toString(),
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
        cantidad = cantidad,
        conteoMarcas = conteoMarcas,
        ajusteManual = ajusteManual,
        marcasJson = marcasJson,
        observaciones = observaciones.trim(),
        estado = estado,
        fotoPath = fotoPath,
        pendienteSync = true,
        actualizadoEn = reloj(),
        // Al reenviar a revisión se limpia la observación del supervisor; en borrador se conserva
        // para que el operador la siga viendo.
        comentarioRevision = if (estado == EstadoRevision.ENVIADA) null else original?.comentarioRevision,
        revisadoPor = original?.revisadoPor
    )
}
