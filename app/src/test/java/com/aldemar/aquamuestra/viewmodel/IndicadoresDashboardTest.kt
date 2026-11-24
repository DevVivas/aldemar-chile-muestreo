package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.util.Fixtures.muestra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndicadoresDashboardTest {

    private val dia = 24L * 60 * 60 * 1000
    private val ahora = 100 * dia

    // 4 muestras: 2 en Calbuco (misma línea), 1 en Huito, 1 antigua (hace 40 días) en Calbuco.
    private val muestras = listOf(
        muestra(id = "a", centro = "Calbuco Sur", tren = "T1", linea = "L1", tramoCm = 100, cantidad = 150,
            estado = EstadoRevision.VALIDADA, fechaHora = ahora - 2 * dia, pendienteSync = false),
        muestra(id = "b", centro = "Calbuco Sur", tren = "T1", linea = "L1", tramoCm = 50, cantidad = 100,
            estado = EstadoRevision.ENVIADA, fechaHora = ahora - 1 * dia, pendienteSync = true),
        muestra(id = "c", centro = "Centro Chiloé (Sector Huito)", tren = "T2", linea = "L5", tramoCm = 200, cantidad = 80,
            estado = EstadoRevision.OBSERVADA, fechaHora = ahora - 10 * dia, pendienteSync = false),
        muestra(id = "d", centro = "Calbuco Sur", tren = "T9", linea = "L9", tramoCm = 0, cantidad = 30,
            estado = EstadoRevision.BORRADOR, fechaHora = ahora - 40 * dia, pendienteSync = true)
    )

    @Test
    fun `dado el periodo todo cuando se calcula entonces suma muestras e individuos de todas`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora)

        // Assert
        assertEquals(4, ind.totalMuestras)
        assertEquals(360, ind.totalIndividuos)
        assertEquals(2, ind.pendientesSync)
    }

    @Test
    fun `dadas densidades 150 200 y 40 cuando se promedia entonces da 130 e ignora el tramo 0`() {
        // Arrange: a = 150/1 m, b = 100/0,5 m = 200, c = 80/2 m = 40, d tiene tramo 0 (no cuenta)

        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora)

        // Assert
        assertEquals(130.0, ind.densidadPromedio!!, 0.0001)
    }

    @Test
    fun `dadas muestras de todos los estados cuando se cuentan entonces trae los 4 y pendientes de revision suma enviadas y observadas`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora)

        // Assert
        assertEquals(EstadoRevision.entries.toList(), ind.porEstado.keys.toList())
        assertEquals(listOf(1, 1, 1, 1), ind.porEstado.values.toList())
        assertEquals(2, ind.pendientesRevision)
    }

    @Test
    fun `dado el periodo de 7 dias cuando se filtra entonces quedan solo las de la ultima semana`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.SIETE_DIAS), ahora)

        // Assert
        assertEquals(2, ind.totalMuestras) // a y b
        assertEquals(0, ind.porEstado[EstadoRevision.OBSERVADA])
    }

    @Test
    fun `dado el periodo de 30 dias cuando se filtra entonces el borde exacto de 30 dias entra y el de 40 no`() {
        // Arrange
        val enElBorde = muestra(id = "borde", fechaHora = ahora - 30 * dia)

        // Act
        val filtradas = IndicadoresDashboard.filtrar(muestras + enElBorde, FiltrosDashboard(PeriodoDashboard.TREINTA_DIAS), ahora)

        // Assert
        assertEquals(listOf("a", "b", "c", "borde"), filtradas.map { it.id })
    }

    @Test
    fun `dado un centro elegido cuando se filtra entonces solo cuenta ese centro`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO, centro = "Centro Chiloé (Sector Huito)"), ahora)

        // Assert
        assertEquals(1, ind.totalMuestras)
        assertEquals(80, ind.totalIndividuos)
    }

    @Test
    fun `dadas muestras de dos centros cuando se agrupan entonces ordena de mayor a menor con sus individuos`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora)

        // Assert
        assertEquals(
            listOf(ResumenCentro("Calbuco Sur", 3, 280), ResumenCentro("Centro Chiloé (Sector Huito)", 1, 80)),
            ind.porCentro
        )
    }

    @Test
    fun `dada ninguna linea elegida cuando se calcula la evolucion entonces usa la mas muestreada ordenada por fecha`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora)

        // Assert
        assertEquals(LineaCultivo("Calbuco Sur", "T1", "L1"), ind.lineaActiva)
        assertEquals("Calbuco Sur · T1 · L1", ind.lineaActiva!!.etiqueta)
        assertEquals(3, ind.lineas.size)
        assertEquals(
            listOf(PuntoLinea(ahora - 2 * dia, 150, 150.0), PuntoLinea(ahora - dia, 100, 200.0)),
            ind.evolucion
        )
    }

    @Test
    fun `dada una linea elegida cuando existe en los filtros entonces grafica esa y si no existe vuelve a la primera`() {
        // Arrange
        val huito = LineaCultivo("Centro Chiloé (Sector Huito)", "T2", "L5")
        val inexistente = LineaCultivo("X", "Y", "Z")

        // Act
        val conHuito = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora, huito)
        val conInexistente = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora, inexistente)

        // Assert
        assertEquals(huito, conHuito.lineaActiva)
        assertEquals(listOf(80), conHuito.evolucion.map { it.cantidad })
        assertEquals(LineaCultivo("Calbuco Sur", "T1", "L1"), conInexistente.lineaActiva)
    }

    @Test
    fun `dada una linea con tramo 0 cuando se grafica entonces su densidad queda vacia`() {
        // Act
        val ind = IndicadoresDashboard.calcular(muestras, FiltrosDashboard(PeriodoDashboard.TODO), ahora, LineaCultivo("Calbuco Sur", "T9", "L9"))

        // Assert
        assertNull(ind.evolucion.single().densidad)
    }

    @Test
    fun `dada una lista vacia cuando se calcula entonces todo queda en cero sin linea`() {
        // Act
        val ind = IndicadoresDashboard.calcular(emptyList(), FiltrosDashboard(), ahora)

        // Assert
        assertEquals(0, ind.totalMuestras)
        assertNull(ind.densidadPromedio)
        assertNull(ind.lineaActiva)
        assertTrue(ind.evolucion.isEmpty())
        assertEquals(0, ind.pendientesRevision)
    }

    @Test
    fun `dadas muestras de varios centros cuando se piden los centros entonces vienen sin repetir y ordenados`() {
        assertEquals(listOf("Calbuco Sur", "Centro Chiloé (Sector Huito)"), IndicadoresDashboard.centros(muestras))
    }
}
