package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity

/** Período del dashboard, contado hacia atrás desde "ahora". */
enum class PeriodoDashboard(val dias: Int?) {
    SIETE_DIAS(7),
    TREINTA_DIAS(30),
    TODO(null)
}

/** Filtros del dashboard. [centro] null = todos los centros. */
data class FiltrosDashboard(
    val periodo: PeriodoDashboard = PeriodoDashboard.TREINTA_DIAS,
    val centro: String? = null
)

/** Una línea de cultivo identificada por centro + tren + línea. */
data class LineaCultivo(val centro: String, val tren: String, val linea: String) {
    val etiqueta: String get() = "$centro · $tren · $linea"
}

/** Muestras e individuos contados en un centro. */
data class ResumenCentro(val centro: String, val muestras: Int, val individuos: Int)

/** Un muestreo de la línea elegida: fecha → cantidad y densidad. */
data class PuntoLinea(val fechaHora: Long, val cantidad: Int, val densidad: Double?)

/** Todo lo que muestra el dashboard, ya calculado. */
data class Indicadores(
    val totalMuestras: Int = 0,
    val totalIndividuos: Int = 0,
    /** Promedio de la densidad (ind/m) de cada muestra. null si ninguna tiene tramo válido. */
    val densidadPromedio: Double? = null,
    /** Siempre trae los 4 estados, en orden de flujo, aunque alguno tenga 0. */
    val porEstado: Map<EstadoRevision, Int> = EstadoRevision.entries.associateWith { 0 },
    /** Muestras guardadas en el teléfono que aún no se suben al servidor. */
    val pendientesSync: Int = 0,
    /** De mayor a menor cantidad de muestras. */
    val porCentro: List<ResumenCentro> = emptyList(),
    /** Líneas disponibles para el gráfico de evolución (de la más muestreada a la menos). */
    val lineas: List<LineaCultivo> = emptyList(),
    /** Línea que se está graficando (la elegida o, si no hay, la más muestreada). */
    val lineaActiva: LineaCultivo? = null,
    /** Evolución de [lineaActiva], de la muestra más antigua a la más nueva. */
    val evolucion: List<PuntoLinea> = emptyList()
) {
    /** Lo que espera al supervisor: enviadas + observadas. */
    val pendientesRevision: Int
        get() = (porEstado[EstadoRevision.ENVIADA] ?: 0) + (porEstado[EstadoRevision.OBSERVADA] ?: 0)
}

/**
 * Cálculo de los indicadores del dashboard. Kotlin puro: no usa Room ni Android,
 * por eso se prueba con JUnit directamente.
 */
object IndicadoresDashboard {

    private const val MS_POR_DIA = 24L * 60 * 60 * 1000

    /** Muestras que entran en los filtros (período + centro). */
    fun filtrar(muestras: List<MuestraEntity>, filtros: FiltrosDashboard, ahora: Long): List<MuestraEntity> {
        val desde = filtros.periodo.dias?.let { ahora - it * MS_POR_DIA }
        return muestras.filter { m ->
            (desde == null || m.fechaHora >= desde) &&
                (filtros.centro == null || m.centro == filtros.centro)
        }
    }

    /**
     * @param muestras todas las muestras de Room.
     * @param ahora hora actual (se recibe para que los tests sean repetibles).
     * @param lineaElegida línea que eligió el usuario para el gráfico de evolución (opcional).
     */
    fun calcular(
        muestras: List<MuestraEntity>,
        filtros: FiltrosDashboard,
        ahora: Long,
        lineaElegida: LineaCultivo? = null
    ): Indicadores {
        val filtradas = filtrar(muestras, filtros, ahora)

        val densidades = filtradas.mapNotNull { densidadPorMetro(it.cantidad, it.tramoCm) }
        val conteoEstados = filtradas.groupingBy { it.estado }.eachCount()

        val porCentro = filtradas.groupBy { it.centro }
            .map { (centro, lista) -> ResumenCentro(centro, lista.size, lista.sumOf { it.cantidad }) }
            .sortedWith(compareByDescending<ResumenCentro> { it.muestras }.thenBy { it.centro })

        val porLinea = filtradas.groupBy { LineaCultivo(it.centro, it.tren, it.linea) }
        val lineas = porLinea.entries
            .sortedWith(compareByDescending<Map.Entry<LineaCultivo, List<MuestraEntity>>> { it.value.size }
                .thenBy { it.key.etiqueta })
            .map { it.key }
        val activa = lineaElegida?.takeIf { it in porLinea } ?: lineas.firstOrNull()
        val evolucion = activa?.let { porLinea.getValue(it) }.orEmpty()
            .sortedBy { it.fechaHora }
            .map { PuntoLinea(it.fechaHora, it.cantidad, densidadPorMetro(it.cantidad, it.tramoCm)) }

        return Indicadores(
            totalMuestras = filtradas.size,
            totalIndividuos = filtradas.sumOf { it.cantidad },
            densidadPromedio = densidades.takeIf { it.isNotEmpty() }?.average(),
            porEstado = EstadoRevision.entries.associateWith { conteoEstados[it] ?: 0 },
            pendientesSync = filtradas.count { it.pendienteSync },
            porCentro = porCentro,
            lineas = lineas,
            lineaActiva = activa,
            evolucion = evolucion
        )
    }

    /** Centros que se pueden elegir en el filtro (de todas las muestras, sin filtrar). */
    fun centros(muestras: List<MuestraEntity>): List<String> = muestras.map { it.centro }.distinct().sorted()
}
