package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision

/** Todo lo que la pantalla "Nueva muestra" necesita para dibujarse. */
data class NewSampleUiState(
    /** UUID de la muestra, generado al abrir el formulario. Solo lectura. */
    val id: String = "",
    /** true mientras se carga una muestra existente para editarla. */
    val cargando: Boolean = false,
    /** Estado que tenía la muestra al abrirla para editar; null = muestra nueva. */
    val estadoOriginal: EstadoRevision? = null,
    val centro: String = "",
    val concesion: String = "",
    val tren: String = "",
    val linea: String = "",
    val operador: String = "",
    val tramoCm: String = "",
    /** Individuos marcados sobre la foto (pantalla de conteo). */
    val conteoMarcas: Int = 0,
    /** Ajuste manual del conteo (puede ser negativo). */
    val ajusteManual: Int = 0,
    /** Posiciones de las marcas (JSON, coordenadas 0..1 de la imagen). */
    val marcasJson: String? = null,
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
) {
    /** Cantidad de individuos = marcados + ajuste. Ya no se escribe a mano: sale del conteo. */
    val cantidad: Int get() = conteoMarcas + ajusteManual
}

sealed interface NewSampleEvento {
    /** La muestra quedó guardada en Room con este estado. */
    data class GuardadoOk(val estado: EstadoRevision) : NewSampleEvento

    /** Hay campos con error: la pantalla avisa que los revise. */
    data object HayErrores : NewSampleEvento

    /** Room lanzó un error al guardar. */
    data object ErrorAlGuardar : NewSampleEvento
}
