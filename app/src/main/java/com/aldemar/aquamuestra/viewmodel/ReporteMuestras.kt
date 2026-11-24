package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Una fila de la tabla del PDF, con los textos ya listos para imprimir. */
data class FilaReporte(
    val id: String,
    val fecha: String,
    val centro: String,
    val tren: String,
    val linea: String,
    val tramoCm: Int,
    val conteo: Int,
    val observaciones: String,
    val estado: String
)

data class TotalesReporte(
    val muestras: Int,
    val individuos: Int,
    /** Suma de los tramos, en metros. */
    val metrosMuestreados: Double,
    /** Promedio de la densidad (ind/m) de cada muestra; null si no hay tramos válidos. */
    val densidadPromedio: Double?
)

/** Todo lo que va en el PDF, ya calculado y formateado. */
data class Reporte(
    val titulo: String,
    val fechaGeneracion: String,
    val generadoPor: String,
    /** Filtros aplicados, ya como texto ("Centro: Calbuco Sur"). */
    val filtros: List<String>,
    val filas: List<FilaReporte>,
    val totales: TotalesReporte
) {
    /** Sin muestras validadas: la pantalla avisa en vez de generar un PDF vacío. */
    val vacio: Boolean get() = filas.isEmpty()
}

/**
 * Prepara el reporte de muestras VALIDADAS (Kotlin puro, sin Android: se prueba con JUnit).
 * El PDF solo dibuja lo que esta función entrega.
 */
object ReporteMuestras {

    const val TITULO = "AquaMuestra · Reporte de muestras validadas"
    const val LARGO_ID = 8
    const val LARGO_MAX_OBSERVACIONES = 60

    private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

    /**
     * @param muestras las que ya pasaron los filtros de la pantalla (se quedan solo las VALIDADA).
     * @param filtros descripción de los filtros aplicados (ver [describir]).
     * @param generadoPor usuario y rol, p. ej. "Supervisor Demo (Supervisor de Calidad)".
     * @param fecha momento de generación (epoch millis).
     */
    fun preparar(
        muestras: List<MuestraEntity>,
        filtros: List<String>,
        generadoPor: String,
        fecha: Long,
        zona: TimeZone = TimeZone.getDefault()
    ): Reporte {
        val validadas = muestras
            .filter { it.estado == EstadoRevision.VALIDADA }
            .sortedBy { it.fechaHora }
        val densidades = validadas.mapNotNull { densidadPorMetro(it.cantidad, it.tramoCm) }

        return Reporte(
            titulo = TITULO,
            fechaGeneracion = formato("dd-MM-yyyy HH:mm", zona).format(Date(fecha)),
            generadoPor = generadoPor,
            filtros = filtros.ifEmpty { listOf("Sin filtros (todas las muestras)") },
            filas = validadas.map { m ->
                FilaReporte(
                    id = m.id.take(LARGO_ID),
                    fecha = formato("dd-MM-yyyy HH:mm", zona).format(Date(m.fechaHora)),
                    centro = m.centro,
                    tren = m.tren,
                    linea = m.linea,
                    tramoCm = m.tramoCm,
                    conteo = m.cantidad,
                    observaciones = recortar(m.observaciones.trim(), LARGO_MAX_OBSERVACIONES),
                    estado = "Validada"
                )
            },
            totales = TotalesReporte(
                muestras = validadas.size,
                individuos = validadas.sumOf { it.cantidad },
                metrosMuestreados = validadas.sumOf { it.tramoCm } / 100.0,
                densidadPromedio = densidades.takeIf { it.isNotEmpty() }?.average()
            )
        )
    }

    /** Corta un texto largo y le agrega "…" para que quepa en la tabla. */
    fun recortar(texto: String, maximo: Int): String =
        if (texto.length <= maximo) texto else texto.take(maximo - 1).trimEnd() + "…"

    /** Nombre sugerido para el archivo: aquamuestra-reporte-AAAAMMDD.pdf. */
    fun nombreArchivo(fecha: Long, zona: TimeZone = TimeZone.getDefault()): String =
        "aquamuestra-reporte-${formato("yyyyMMdd", zona).format(Date(fecha))}.pdf"

    /** Filtros del dashboard como texto para el encabezado del PDF. */
    fun describir(filtros: FiltrosDashboard): List<String> = listOf(
        "Período: " + when (filtros.periodo) {
            PeriodoDashboard.SIETE_DIAS -> "últimos 7 días"
            PeriodoDashboard.TREINTA_DIAS -> "últimos 30 días"
            PeriodoDashboard.TODO -> "todo"
        },
        "Centro: " + (filtros.centro ?: "todos")
    )

    /** Filtros del historial como texto. Las fechas vienen en medianoche UTC (DateRangePicker). */
    fun describir(filtros: FiltrosHistorial): List<String> = buildList {
        filtros.centro?.let { add("Centro: $it") }
        filtros.tren?.let { add("Tren: $it") }
        filtros.linea?.let { add("Línea: $it") }
        filtros.estado?.let { add("Estado: " + it.name.lowercase().replaceFirstChar(Char::titlecase)) }
        if (filtros.desdeUtc != null || filtros.hastaUtc != null) {
            val f = formato("dd-MM-yyyy", TimeZone.getTimeZone("UTC"))
            val desde = filtros.desdeUtc?.let { f.format(Date(it)) } ?: "inicio"
            val hasta = filtros.hastaUtc?.let { f.format(Date(it)) } ?: "hoy"
            add("Fechas: $desde a $hasta")
        }
    }

    private fun formato(patron: String, zona: TimeZone) =
        SimpleDateFormat(patron, LOCALE_CL).apply { timeZone = zona }
}
