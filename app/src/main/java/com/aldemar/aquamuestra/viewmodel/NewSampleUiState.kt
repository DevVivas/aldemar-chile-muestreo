package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision

/** Todo lo que la pantalla "Nueva muestra" necesita para dibujarse. */
data class NewSampleUiState(
    /** UUID de la muestra, generado al abrir el formulario. Solo lectura. */
    val id: String = "",
    val centro: String = "",
    val concesion: String = "",
    val tren: String = "",
    val linea: String = "",
    val operador: String = "",
    val tramoCm: String = "",
    val cantidad: String = "0",
    val observaciones: String = "",
    /** Fecha y hora de la muestra en milisegundos (epoch millis). */
    val fechaHora: Long = 0L,
    /** Ruta del archivo de la foto, o null si aún no hay foto. */
    val fotoPath: String? = null,
    val errores: ErroresFormulario = ErroresFormulario(),
    val isSaving: Boolean = false,
    /**
     * Último intento de guardado que falló por validación. Mientras no sea null,
     * los errores se recalculan al editar, para que desaparezcan apenas se corrigen.
     */
    val intentoFallido: EstadoRevision? = null,
    /** Evento de un solo uso para la pantalla (Snackbar, volver atrás). */
    val evento: NewSampleEvento? = null
)

sealed interface NewSampleEvento {
    /** La muestra quedó guardada en Room con este estado. */
    data class GuardadoOk(val estado: EstadoRevision) : NewSampleEvento

    /** Hay campos con error: la pantalla avisa que los revise. */
    data object HayErrores : NewSampleEvento

    /** Room lanzó un error al guardar. */
    data object ErrorAlGuardar : NewSampleEvento
}
