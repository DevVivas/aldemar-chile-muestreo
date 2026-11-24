package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.util.Fixtures.muestra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccionesRevisionTest {

    @Test
    fun `dadas todas las combinaciones de rol y estado cuando se piden acciones entonces cada una es la esperada`() {
        // Arrange: tabla completa rol × estado (incluye ANALISTA y sin sesión)
        val esperado = mapOf(
            (UserRole.SUPERVISOR to EstadoRevision.BORRADOR) to emptyList(),
            (UserRole.SUPERVISOR to EstadoRevision.ENVIADA) to listOf(AccionRevision.OBSERVAR, AccionRevision.VALIDAR),
            (UserRole.SUPERVISOR to EstadoRevision.OBSERVADA) to emptyList(),
            (UserRole.SUPERVISOR to EstadoRevision.VALIDADA) to emptyList(),
            (UserRole.OPERARIO to EstadoRevision.BORRADOR) to listOf(AccionRevision.EDITAR),
            (UserRole.OPERARIO to EstadoRevision.ENVIADA) to emptyList(),
            (UserRole.OPERARIO to EstadoRevision.OBSERVADA) to listOf(AccionRevision.EDITAR),
            (UserRole.OPERARIO to EstadoRevision.VALIDADA) to emptyList(),
            (UserRole.ANALISTA to EstadoRevision.BORRADOR) to emptyList(),
            (UserRole.ANALISTA to EstadoRevision.ENVIADA) to emptyList(),
            (UserRole.ANALISTA to EstadoRevision.OBSERVADA) to emptyList(),
            (UserRole.ANALISTA to EstadoRevision.VALIDADA) to emptyList(),
            (null to EstadoRevision.ENVIADA) to emptyList<AccionRevision>()
        )

        // Act + Assert (la muestra es del mismo usuario que tiene la sesión)
        esperado.forEach { (entrada, acciones) ->
            val (rol, estado) = entrada
            assertEquals("rol=$rol estado=$estado", acciones, AccionesRevision.disponibles(rol, estado, "Operario Demo", "Operario Demo"))
        }
    }

    @Test
    fun `dada una muestra de otro operario cuando la abre un operario entonces no puede editarla ni corregirla`() {
        // Arrange: muestra registrada por "Operario Norte"; sesión de "Operario Demo"
        val otro = "Operario Norte"

        // Act + Assert
        assertTrue(AccionesRevision.disponibles(UserRole.OPERARIO, EstadoRevision.BORRADOR, otro, "Operario Demo").isEmpty())
        assertTrue(AccionesRevision.disponibles(UserRole.OPERARIO, EstadoRevision.OBSERVADA, otro, "Operario Demo").isEmpty())
        assertTrue(AccionesRevision.disponibles(UserRole.OPERARIO, EstadoRevision.OBSERVADA, otro, null).isEmpty())
    }

    @Test
    fun `dado el mismo nombre con otras mayusculas o espacios cuando se compara entonces es del usuario`() {
        assertTrue(AccionesRevision.esDelUsuario(" operario demo ", "Operario Demo"))
        assertFalse(AccionesRevision.esDelUsuario("Operario Demo", null))
        assertEquals(
            listOf(AccionRevision.EDITAR),
            AccionesRevision.disponibles(UserRole.OPERARIO, EstadoRevision.OBSERVADA, "OPERARIO DEMO", "Operario Demo")
        )
    }

    @Test
    fun `dada una muestra de otro operario cuando la abre un supervisor entonces igual puede revisarla`() {
        assertEquals(
            listOf(AccionRevision.OBSERVAR, AccionRevision.VALIDAR),
            AccionesRevision.disponibles(UserRole.SUPERVISOR, EstadoRevision.ENVIADA, "Operario Norte", "Supervisor Demo")
        )
    }

    @Test
    fun `dado un operario cuando la muestra esta enviada entonces no puede validar`() {
        assertFalse(AccionRevision.VALIDAR in AccionesRevision.disponibles(UserRole.OPERARIO, EstadoRevision.ENVIADA, "Operario Demo", "Operario Demo"))
    }

    @Test
    fun `dado un comentario cuando se valida entonces exige texto y maximo 300 caracteres`() {
        assertFalse(AccionesRevision.comentarioValido("   "))
        assertTrue(AccionesRevision.comentarioValido("Repetir conteo"))
        assertTrue(AccionesRevision.comentarioValido("a".repeat(300)))
        assertFalse(AccionesRevision.comentarioValido("a".repeat(301)))
    }

    @Test
    fun `dado un conteo y un tramo cuando se calcula la densidad entonces da individuos por metro`() {
        assertEquals(150.0, densidadPorMetro(cantidad = 150, tramoCm = 100)!!, 0.001)
        assertEquals(250.0, densidadPorMetro(cantidad = 200, tramoCm = 80)!!, 0.001)
    }

    @Test
    fun `dado un tramo de 0 cuando se calcula la densidad entonces no hay valor`() {
        assertNull(densidadPorMetro(cantidad = 10, tramoCm = 0))
    }

    @Test
    fun `dados tres muestreos de una linea cuando se calcula la evolucion entonces trae densidad y variacion`() {
        // Arrange
        val muestras = listOf(
            muestra(id = "1", cantidad = 100, tramoCm = 100), // 100 ind/m
            muestra(id = "2", cantidad = 150, tramoCm = 100), // 150 ind/m → +50 %
            muestra(id = "3", cantidad = 120, tramoCm = 100)  // 120 ind/m → −20 %
        )

        // Act
        val puntos = EvolucionLinea.calcular(muestras, idActual = "2")

        // Assert
        assertNull(puntos[0].variacionPct)
        assertEquals(50.0, puntos[1].variacionPct!!, 0.001)
        assertEquals(-20.0, puntos[2].variacionPct!!, 0.001)
        assertEquals(listOf(false, true, false), puntos.map { it.esActual })
    }

    @Test
    fun `dado un muestreo anterior sin densidad cuando se calcula la evolucion entonces no hay variacion`() {
        // Arrange: el primero tiene tramo 0 (sin densidad) y el segundo densidad 0
        val muestras = listOf(
            muestra(id = "1", cantidad = 10, tramoCm = 0),
            muestra(id = "2", cantidad = 0, tramoCm = 100),
            muestra(id = "3", cantidad = 50, tramoCm = 100)
        )

        // Act
        val puntos = EvolucionLinea.calcular(muestras, idActual = "3")

        // Assert
        assertNull(puntos[0].densidad)
        assertNull(puntos[1].variacionPct) // anterior sin densidad
        assertNull(puntos[2].variacionPct) // anterior con densidad 0: no se divide por cero
    }
}
