package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import java.util.Calendar
import java.util.TimeZone

/**
 * Filtros del historial. null = "sin filtro" para ese campo.
 *
 * Las fechas se guardan tal como las entrega el DateRangePicker de Material 3:
 * medianoche en UTC del día elegido.
 */
data class FiltrosHistorial(
    val centro: String? = null,
    val tren: String? = null,
    val linea: String? = null,
    val estado: EstadoRevision? = null,
    val desdeUtc: Long? = null,
    val hastaUtc: Long? = null
) {
    val hayFiltros: Boolean
        get() = centro != null || tren != null || linea != null || estado != null ||
            desdeUtc != null || hastaUtc != null
}

/** Valores que se pueden elegir en cada filtro (sacados de las muestras guardadas). */
data class OpcionesFiltro(
    val centros: List<String> = emptyList(),
    val trenes: List<String> = emptyList(),
    val lineas: List<String> = emptyList(),
    val estados: List<EstadoRevision> = emptyList()
)

/**
 * Lógica de filtrado del historial. Kotlin puro: no depende de Android ni de Compose,
 * por eso se prueba con JUnit.
 */
object FiltroMuestras {

    /**
     * Devuelve solo las muestras que cumplen TODOS los filtros activos (se combinan con "Y").
     * Mantiene el orden de [lista].
     *
     * @param zona zona horaria del teléfono; se usa para saber a qué día local pertenece cada muestra.
     */
    fun aplicar(
        lista: List<MuestraEntity>,
        filtros: FiltrosHistorial,
        zona: TimeZone = TimeZone.getDefault()
    ): List<MuestraEntity> {
        val desde = filtros.desdeUtc?.let { inicioDelDiaLocal(it, zona) }
        val hastaExclusivo = filtros.hastaUtc?.let { inicioDelDiaLocal(it, zona, sumarDias = 1) }

        return lista.filter { m ->
            (filtros.centro == null || m.centro == filtros.centro) &&
                (filtros.tren == null || m.tren == filtros.tren) &&
                (filtros.linea == null || m.linea == filtros.linea) &&
                (filtros.estado == null || m.estado == filtros.estado) &&
                (desde == null || m.fechaHora >= desde) &&
                (hastaExclusivo == null || m.fechaHora < hastaExclusivo)
        }
    }

    /** Opciones de cada filtro a partir de TODAS las muestras (sin repetir, ordenadas). */
    fun opciones(lista: List<MuestraEntity>): OpcionesFiltro = OpcionesFiltro(
        centros = lista.map { it.centro }.distinct().sorted(),
        trenes = lista.map { it.tren }.distinct().sorted(),
        lineas = lista.map { it.linea }.distinct().sorted(),
        estados = lista.map { it.estado }.distinct().sortedBy { it.ordinal }
    )

    /**
     * Convierte "medianoche UTC del día X" (lo que da el DatePicker) en
     * "medianoche local del día X" en epoch millis.
     */
    private fun inicioDelDiaLocal(fechaUtc: Long, zona: TimeZone, sumarDias: Int = 0): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = fechaUtc }
        return Calendar.getInstance(zona).apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
            add(Calendar.DAY_OF_MONTH, sumarDias)
        }.timeInMillis
    }
}
