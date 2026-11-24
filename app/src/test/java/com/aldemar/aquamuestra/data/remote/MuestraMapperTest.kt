package com.aldemar.aquamuestra.data.remote

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.util.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class MuestraMapperTest {

    @Test
    fun `dada una muestra local cuando va al servidor y vuelve entonces conserva sus datos`() {
        // Arrange: fecha exacta al segundo (el formato ISO no guarda milisegundos)
        val fecha = MuestraMapper.desdeIso("2026-11-20T10:30:00")
        val local = Fixtures.muestra(
            fechaHora = fecha, cantidad = 12, conteoMarcas = 10, ajusteManual = 2,
            estado = EstadoRevision.OBSERVADA, comentarioRevision = "Repetir", revisadoPor = "Supervisor Demo",
            fotoPath = "/fotos/m-1.jpg", marcasJson = "[]"
        )

        // Act
        val dto = MuestraMapper.aDto(local, fotoUrl = "/api/muestras/m-1/foto")
        val vuelta = MuestraMapper.aEntidad(dto, fotoPathLocal = local.fotoPath, marcasJsonLocal = local.marcasJson)

        // Assert
        assertEquals("2026-11-20T10:30:00", dto.fechaHora)
        assertNull(dto.actualizadoEn) // lo pone el servidor
        assertEquals(local.copy(pendienteSync = false, actualizadoEn = vuelta.actualizadoEn), vuelta)
        assertFalse(vuelta.pendienteSync)
    }

    @Test
    fun `dada una muestra antigua sin desglose cuando llega del servidor entonces todo el total queda como ajuste`() {
        // Act
        val e = MuestraMapper.aEntidad(Fixtures.dto(cantidad = 180, conteoMarcas = null, ajusteManual = null))

        // Assert
        assertEquals(0, e.conteoMarcas)
        assertEquals(180, e.ajusteManual)
        assertEquals(e.cantidad, e.conteoMarcas + e.ajusteManual)
    }

    @Test
    fun `dada una fecha con fracciones de segundo cuando se lee entonces se ignoran`() {
        assertEquals(
            MuestraMapper.desdeIso("2026-11-20T10:31:05"),
            MuestraMapper.desdeIso("2026-11-20T10:31:05.123456")
        )
    }

    @Test
    fun `dada una muestra del servidor sin actualizadoEn cuando se convierte entonces usa la hora actual`() {
        // Act
        val antes = System.currentTimeMillis()
        val e = MuestraMapper.aEntidad(Fixtures.dto().copy(actualizadoEn = null, observaciones = null))

        // Assert
        assertEquals("", e.observaciones)
        assert(e.actualizadoEn >= antes)
    }
}
