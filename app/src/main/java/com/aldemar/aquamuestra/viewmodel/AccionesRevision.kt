package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.local.UserRole

/** Acciones que se pueden hacer sobre una muestra desde el detalle. */
enum class AccionRevision {
    /** Supervisor: aprobar la muestra. */
    VALIDAR,

    /** Supervisor: devolverla al operador con un comentario. */
    OBSERVAR,

    /** Operario: abrir la muestra en el formulario (editar borrador o corregir observada). */
    EDITAR
}

/**
 * Reglas de quién puede hacer qué, según su rol y el estado de la muestra.
 * Kotlin puro: se prueba con JUnit sin emulador.
 */
object AccionesRevision {

    const val MAX_COMENTARIO = 300

    fun disponibles(rol: UserRole?, estado: EstadoRevision): List<AccionRevision> = when {
        // Una muestra validada queda cerrada: solo lectura para todos.
        estado == EstadoRevision.VALIDADA -> emptyList()

        // El analista solo consulta: nunca crea, edita ni revisa.
        rol == UserRole.ANALISTA -> emptyList()

        rol == UserRole.SUPERVISOR && estado == EstadoRevision.ENVIADA ->
            listOf(AccionRevision.OBSERVAR, AccionRevision.VALIDAR)

        rol == UserRole.OPERARIO &&
            (estado == EstadoRevision.BORRADOR || estado == EstadoRevision.OBSERVADA) ->
            listOf(AccionRevision.EDITAR)

        else -> emptyList()
    }

    /** Observar exige un comentario con texto (y no más largo de lo permitido). */
    fun comentarioValido(comentario: String): Boolean =
        comentario.isNotBlank() && comentario.trim().length <= MAX_COMENTARIO
}

/** Individuos por metro de cuerda: cantidad / tramoCm * 100. null si el tramo no es válido. */
fun densidadPorMetro(cantidad: Int, tramoCm: Int): Double? =
    if (tramoCm <= 0) null else cantidad * 100.0 / tramoCm

/** Un muestreo dentro de la evolución de una línea. */
data class PuntoEvolucion(
    val id: String,
    val fechaHora: Long,
    val cantidad: Int,
    val densidad: Double?,
    /** Diferencia de densidad con el muestreo anterior, en %. null en el primero o si no se puede calcular. */
    val variacionPct: Double?,
    val esActual: Boolean
)

object EvolucionLinea {

    /**
     * Convierte los muestreos de una línea (ordenados del más antiguo al más nuevo)
     * en puntos con densidad y variación respecto al anterior.
     */
    fun calcular(muestras: List<MuestraEntity>, idActual: String): List<PuntoEvolucion> {
        var densidadAnterior: Double? = null
        return muestras.map { m ->
            val densidad = densidadPorMetro(m.cantidad, m.tramoCm)
            val anterior = densidadAnterior
            val variacion = if (densidad != null && anterior != null && anterior > 0.0) {
                (densidad - anterior) / anterior * 100.0
            } else {
                null
            }
            densidadAnterior = densidad
            PuntoEvolucion(m.id, m.fechaHora, m.cantidad, densidad, variacion, m.id == idActual)
        }
    }
}
