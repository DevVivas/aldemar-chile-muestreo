package com.aldemar.aquamuestra.viewmodel

/**
 * Tipo de error de un campo. Es un enum (y no un texto) para que el validador
 * no dependa de Android: la pantalla traduce cada valor a un mensaje en strings.xml.
 */
enum class ErrorCampo {
    OBLIGATORIO,
    ENTERO_MAYOR_A_CERO,
    ENTERO_CERO_O_MAS,
    DEMASIADO_LARGO,
    FOTO_OBLIGATORIA
}

/** Error de cada campo del formulario. null = el campo está bien. */
data class ErroresFormulario(
    val centro: ErrorCampo? = null,
    val tren: ErrorCampo? = null,
    val linea: ErrorCampo? = null,
    val operador: ErrorCampo? = null,
    val tramoCm: ErrorCampo? = null,
    val cantidad: ErrorCampo? = null,
    val observaciones: ErrorCampo? = null,
    val foto: ErrorCampo? = null
) {
    val hayErrores: Boolean
        get() = listOf(centro, tren, linea, operador, tramoCm, cantidad, observaciones, foto)
            .any { it != null }
}

/**
 * Reglas del formulario de muestra. Es Kotlin puro (sin Android), por eso se puede
 * probar con JUnit sin emulador.
 */
object MuestraFormValidator {

    const val MAX_OBSERVACIONES = 300

    /**
     * @param paraEnviar true al "Enviar a revisión" (exige foto); false al guardar borrador.
     */
    fun validar(
        centro: String,
        tren: String,
        linea: String,
        operador: String,
        tramoCm: String,
        cantidad: String,
        observaciones: String,
        fotoPath: String?,
        paraEnviar: Boolean
    ): ErroresFormulario = ErroresFormulario(
        centro = obligatorio(centro),
        tren = obligatorio(tren),
        linea = obligatorio(linea),
        operador = obligatorio(operador),
        tramoCm = when {
            tramoCm.isBlank() -> ErrorCampo.OBLIGATORIO
            (tramoCm.trim().toIntOrNull() ?: 0) <= 0 -> ErrorCampo.ENTERO_MAYOR_A_CERO
            else -> null
        },
        cantidad = when {
            cantidad.isBlank() -> ErrorCampo.OBLIGATORIO
            (cantidad.trim().toIntOrNull() ?: -1) < 0 -> ErrorCampo.ENTERO_CERO_O_MAS
            else -> null
        },
        observaciones = if (observaciones.length > MAX_OBSERVACIONES) ErrorCampo.DEMASIADO_LARGO else null,
        foto = if (paraEnviar && fotoPath.isNullOrBlank()) ErrorCampo.FOTO_OBLIGATORIA else null
    )

    private fun obligatorio(valor: String): ErrorCampo? =
        if (valor.isBlank()) ErrorCampo.OBLIGATORIO else null
}
