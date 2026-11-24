package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.util.Fixtures.muestra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReporteMuestrasTest {

    private val santiago = TimeZone.getTimeZone("America/Santiago")

    /** 20-11-2026 10:30 en Santiago. */
    private val fecha = Calendar.getInstance(santiago).apply { clear(); set(2026, Calendar.NOVEMBER, 20, 10, 30) }.timeInMillis

    @Test
    fun `dadas muestras de todos los estados cuando se prepara entonces solo quedan las validadas`() {
        // Arrange
        val muestras = EstadoRevision.entries.map { muestra(id = "id-${it.name}", estado = it) }

        // Act
        val r = ReporteMuestras.preparar(muestras, emptyList(), "Supervisor Demo (Supervisor de Calidad)", fecha, santiago)

        // Assert
        assertEquals(listOf("id-VALID"), r.filas.map { it.id }) // ID recortado a 8 caracteres
        assertEquals("Validada", r.filas.single().estado)
        assertFalse(r.vacio)
    }

    @Test
    fun `dada una muestra validada cuando se arma la fila entonces trae todos sus datos formateados`() {
        // Arrange
        val m = muestra(
            id = "0f9e8d7c-6b5a-4321-9876-abcdef012345", centro = "Calbuco Sur", tren = "Tren 1 - Módulo A",
            linea = "Línea 10", tramoCm = 80, cantidad = 120, fechaHora = fecha, estado = EstadoRevision.VALIDADA,
            observaciones = "  Buena fijación  "
        )

        // Act
        val fila = ReporteMuestras.preparar(listOf(m), emptyList(), "x", fecha, santiago).filas.single()

        // Assert
        assertEquals(
            FilaReporte("0f9e8d7c", "20-11-2026 10:30", "Calbuco Sur", "Tren 1 - Módulo A", "Línea 10", 80, 120, "Buena fijación", "Validada"),
            fila
        )
    }

    @Test
    fun `dadas observaciones de 200 caracteres cuando se prepara entonces se recortan a 60 con puntos suspensivos`() {
        // Arrange
        val larga = "a".repeat(200)

        // Act
        val obs = ReporteMuestras.preparar(listOf(muestra(estado = EstadoRevision.VALIDADA, observaciones = larga)), emptyList(), "x", fecha, santiago)
            .filas.single().observaciones

        // Assert
        assertEquals(60, obs.length)
        assertTrue(obs.endsWith("…"))
        assertEquals("corto", ReporteMuestras.recortar("corto", 60))
    }

    @Test
    fun `dadas tres validadas cuando se prepara entonces calcula totales y ordena por fecha`() {
        // Arrange: densidades 100, 50 y 200 ind/m
        val muestras = listOf(
            muestra(id = "tercera", estado = EstadoRevision.VALIDADA, fechaHora = 3L, cantidad = 200, tramoCm = 100),
            muestra(id = "primera", estado = EstadoRevision.VALIDADA, fechaHora = 1L, cantidad = 100, tramoCm = 100),
            muestra(id = "segunda", estado = EstadoRevision.VALIDADA, fechaHora = 2L, cantidad = 25, tramoCm = 50),
            muestra(id = "enviada", estado = EstadoRevision.ENVIADA, cantidad = 999)
        )

        // Act
        val r = ReporteMuestras.preparar(muestras, emptyList(), "x", fecha, santiago)

        // Assert
        assertEquals(listOf("primera", "segunda", "tercera"), r.filas.map { it.id })
        assertEquals(TotalesReporte(muestras = 3, individuos = 325, metrosMuestreados = 2.5, densidadPromedio = 116.66666666666667), r.totales)
    }

    @Test
    fun `dado que no hay validadas cuando se prepara entonces el reporte queda vacio para avisar`() {
        // Act
        val r = ReporteMuestras.preparar(listOf(muestra(estado = EstadoRevision.ENVIADA)), listOf("Centro: X"), "x", fecha, santiago)

        // Assert
        assertTrue(r.vacio)
        assertEquals(0, r.totales.muestras)
        assertNull(r.totales.densidadPromedio)
    }

    @Test
    fun `dado un usuario y una fecha cuando se prepara entonces el encabezado los trae y sin filtros lo dice`() {
        // Act
        val r = ReporteMuestras.preparar(emptyList(), emptyList(), "Analista Demo (Analista de Datos)", fecha, santiago)

        // Assert
        assertEquals("AquaMuestra · Reporte de muestras validadas", r.titulo)
        assertEquals("20-11-2026 10:30", r.fechaGeneracion)
        assertEquals("Analista Demo (Analista de Datos)", r.generadoPor)
        assertEquals(listOf("Sin filtros (todas las muestras)"), r.filtros)
    }

    @Test
    fun `dada una fecha cuando se pide el nombre del archivo entonces es aquamuestra-reporte-AAAAMMDD`() {
        assertEquals("aquamuestra-reporte-20261120.pdf", ReporteMuestras.nombreArchivo(fecha, santiago))
    }

    @Test
    fun `dados los filtros del dashboard cuando se describen entonces dice periodo y centro`() {
        assertEquals(listOf("Período: últimos 7 días", "Centro: todos"), ReporteMuestras.describir(FiltrosDashboard(PeriodoDashboard.SIETE_DIAS)))
        assertEquals(listOf("Período: últimos 30 días", "Centro: Calbuco Sur"), ReporteMuestras.describir(FiltrosDashboard(PeriodoDashboard.TREINTA_DIAS, "Calbuco Sur")))
        assertEquals("Período: todo", ReporteMuestras.describir(FiltrosDashboard(PeriodoDashboard.TODO)).first())
    }

    @Test
    fun `dados los filtros del historial cuando se describen entonces lista cada uno y el rango de fechas`() {
        // Arrange: 01-11-2026 y 20-11-2026 a medianoche UTC (lo que entrega el DateRangePicker)
        val utc = TimeZone.getTimeZone("UTC")
        val desde = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.NOVEMBER, 1) }.timeInMillis
        val hasta = Calendar.getInstance(utc).apply { clear(); set(2026, Calendar.NOVEMBER, 20) }.timeInMillis

        // Act
        val todos = ReporteMuestras.describir(FiltrosHistorial("Calbuco Sur", "T1", "L1", EstadoRevision.VALIDADA, desde, hasta))
        val soloDesde = ReporteMuestras.describir(FiltrosHistorial(desdeUtc = desde))
        val soloHasta = ReporteMuestras.describir(FiltrosHistorial(hastaUtc = hasta))

        // Assert
        assertEquals(
            listOf("Centro: Calbuco Sur", "Tren: T1", "Línea: L1", "Estado: Validada", "Fechas: 01-11-2026 a 20-11-2026"),
            todos
        )
        assertEquals(listOf("Fechas: 01-11-2026 a hoy"), soloDesde)
        assertEquals(listOf("Fechas: inicio a 20-11-2026"), soloHasta)
        assertTrue(ReporteMuestras.describir(FiltrosHistorial()).isEmpty())
    }
}
