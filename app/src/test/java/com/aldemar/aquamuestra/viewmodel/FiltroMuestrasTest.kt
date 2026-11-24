package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.util.Fixtures.muestra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FiltroMuestrasTest {

    private val chile = TimeZone.getTimeZone("America/Santiago")

    /** Milisegundos de una fecha y hora LOCAL en la zona indicada. */
    private fun local(anio: Int, mes: Int, dia: Int, hora: Int, minuto: Int = 0, zona: TimeZone = chile): Long =
        Calendar.getInstance(zona).apply {
            clear()
            set(anio, mes - 1, dia, hora, minuto)
        }.timeInMillis

    /** Medianoche UTC de un día (lo que entrega el DateRangePicker). */
    private fun diaUtc(anio: Int, mes: Int, dia: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(anio, mes - 1, dia)
        }.timeInMillis

    private val lista = listOf(
        muestra(id = "a", centro = "Calbuco Sur", tren = "T1", linea = "L10", estado = EstadoRevision.VALIDADA, fechaHora = local(2026, 11, 10, 9)),
        muestra(id = "b", centro = "Calbuco Sur", tren = "T2", linea = "L15", estado = EstadoRevision.ENVIADA, fechaHora = local(2026, 11, 15, 9)),
        muestra(id = "c", centro = "Huito", tren = "T1", linea = "L10", estado = EstadoRevision.OBSERVADA, fechaHora = local(2026, 11, 20, 9))
    )

    private fun ids(r: List<com.aldemar.aquamuestra.data.local.MuestraEntity>) = r.map { it.id }

    @Test
    fun `dados filtros vacios cuando se aplica entonces devuelve todas en el mismo orden`() {
        assertEquals(listOf("a", "b", "c"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(), chile)))
    }

    @Test
    fun `dado un centro cuando se aplica entonces solo quedan las de ese centro`() {
        assertEquals(listOf("a", "b"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(centro = "Calbuco Sur"), chile)))
    }

    @Test
    fun `dado un tren cuando se aplica entonces solo quedan las de ese tren`() {
        assertEquals(listOf("a", "c"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(tren = "T1"), chile)))
    }

    @Test
    fun `dada una linea cuando se aplica entonces solo quedan las de esa linea`() {
        assertEquals(listOf("b"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(linea = "L15"), chile)))
    }

    @Test
    fun `dado un estado cuando se aplica entonces solo quedan las de ese estado`() {
        assertEquals(listOf("c"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(estado = EstadoRevision.OBSERVADA), chile)))
    }

    @Test
    fun `dados centro y tren cuando se aplica entonces deben cumplirse los dos`() {
        // Act
        val r = FiltroMuestras.aplicar(lista, FiltrosHistorial(centro = "Calbuco Sur", tren = "T1"), chile)

        // Assert
        assertEquals(listOf("a"), ids(r))
    }

    @Test
    fun `dados filtros que no coinciden cuando se aplica entonces la lista queda vacia`() {
        assertTrue(FiltroMuestras.aplicar(lista, FiltrosHistorial(centro = "Huito", estado = EstadoRevision.VALIDADA), chile).isEmpty())
    }

    @Test
    fun `dado un rango de fechas cuando se aplica entonces incluye los dias extremos completos`() {
        // Arrange: muestras justo en los bordes del día 15 (hora local de Chile)
        val bordes = listOf(
            muestra(id = "antes", fechaHora = local(2026, 11, 14, 23, 59)),
            muestra(id = "inicio", fechaHora = local(2026, 11, 15, 0, 0)),
            muestra(id = "fin", fechaHora = local(2026, 11, 15, 23, 59)),
            muestra(id = "despues", fechaHora = local(2026, 11, 16, 0, 0))
        )

        // Act: el rango es solo el 15 de noviembre
        val r = FiltroMuestras.aplicar(bordes, FiltrosHistorial(desdeUtc = diaUtc(2026, 11, 15), hastaUtc = diaUtc(2026, 11, 15)), chile)

        // Assert
        assertEquals(listOf("inicio", "fin"), ids(r))
    }

    @Test
    fun `dado solo desde cuando se aplica entonces no hay limite superior`() {
        assertEquals(listOf("b", "c"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(desdeUtc = diaUtc(2026, 11, 15)), chile)))
    }

    @Test
    fun `dado solo hasta cuando se aplica entonces no hay limite inferior`() {
        assertEquals(listOf("a", "b"), ids(FiltroMuestras.aplicar(lista, FiltrosHistorial(hastaUtc = diaUtc(2026, 11, 15)), chile)))
    }

    @Test
    fun `dada otra zona horaria cuando se aplica entonces el dia se calcula en hora local de esa zona`() {
        // Arrange: 22:00 del día 15 en Chile = 01:00 del día 16 en UTC
        val noche = listOf(muestra(id = "noche", fechaHora = local(2026, 11, 15, 22)))
        val filtroDia15 = FiltrosHistorial(desdeUtc = diaUtc(2026, 11, 15), hastaUtc = diaUtc(2026, 11, 15))

        // Act
        val enChile = FiltroMuestras.aplicar(noche, filtroDia15, chile)
        val enUtc = FiltroMuestras.aplicar(noche, filtroDia15, TimeZone.getTimeZone("UTC"))

        // Assert: en Chile pertenece al día 15; en UTC ya es el 16
        assertEquals(listOf("noche"), ids(enChile))
        assertTrue(enUtc.isEmpty())
    }

    @Test
    fun `dadas muestras cuando se piden opciones entonces vienen sin repetir y ordenadas`() {
        // Act
        val o = FiltroMuestras.opciones(lista)

        // Assert
        assertEquals(listOf("Calbuco Sur", "Huito"), o.centros)
        assertEquals(listOf("T1", "T2"), o.trenes)
        assertEquals(listOf("L10", "L15"), o.lineas)
        assertEquals(listOf(EstadoRevision.ENVIADA, EstadoRevision.OBSERVADA, EstadoRevision.VALIDADA), o.estados)
    }

    @Test
    fun `dados filtros cuando se pregunta si hay filtros entonces responde segun haya alguno activo`() {
        assertFalse(FiltrosHistorial().hayFiltros)
        assertTrue(FiltrosHistorial(linea = "L10").hayFiltros)
        assertTrue(FiltrosHistorial(hastaUtc = 1L).hayFiltros)
    }
}
