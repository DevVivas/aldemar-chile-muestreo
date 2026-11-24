package com.aldemar.aquamuestra.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ConteoMarcasTest {

    private val tol = 0.05f // tolerancia de toque (5 % del ancho y del alto)

    @Test
    fun `dado un conteo vacio cuando se agrega una marca dentro de la foto entonces hay una marca`() {
        // Act
        val c = ConteoMarcas().agregar(0.3f, 0.6f)

        // Assert
        assertEquals(listOf(Punto(0.3f, 0.6f)), c.marcas)
        assertEquals(1, c.total)
    }

    @Test
    fun `dado un toque fuera de la foto cuando se agrega entonces no cambia nada`() {
        // Arrange
        val c = ConteoMarcas()

        // Act + Assert
        assertSame(c, c.agregar(-0.1f, 0.5f))
        assertSame(c, c.agregar(0.5f, 1.2f))
    }

    @Test
    fun `dado un toque sobre una marca cuando se toca entonces la quita`() {
        // Arrange
        val c = ConteoMarcas().agregar(0.5f, 0.5f)

        // Act
        val (nuevo, agregada) = c.tocar(0.51f, 0.49f, tol, tol)

        // Assert
        assertFalse(agregada)
        assertTrue(nuevo.marcas.isEmpty())
    }

    @Test
    fun `dado un toque lejos de las marcas cuando se toca entonces agrega una nueva`() {
        // Arrange
        val c = ConteoMarcas().agregar(0.1f, 0.1f)

        // Act
        val (nuevo, agregada) = c.tocar(0.8f, 0.8f, tol, tol)

        // Assert
        assertTrue(agregada)
        assertEquals(2, nuevo.conteoMarcas)
    }

    @Test
    fun `dadas dos marcas cercanas cuando se busca la mas cercana entonces elige la de menor distancia`() {
        // Arrange
        val c = ConteoMarcas(listOf(Punto(0.50f, 0.50f), Punto(0.53f, 0.50f)))

        // Act + Assert
        assertEquals(1, c.indiceMasCercano(0.525f, 0.50f, tol, tol))
        assertNull(c.indiceMasCercano(0.5f, 0.5f, 0f, tol)) // tolerancia inválida
    }

    @Test
    fun `dadas marcas cuando se deshace entonces se quita la ultima agregada`() {
        // Arrange
        val c = ConteoMarcas().agregar(0.1f, 0.1f).agregar(0.2f, 0.2f)

        // Act
        val d = c.deshacer()

        // Assert
        assertEquals(listOf(Punto(0.1f, 0.1f)), d.marcas)
    }

    @Test
    fun `dado un conteo vacio cuando se deshace entonces queda igual`() {
        val vacio = ConteoMarcas(ajusteManual = 2)
        assertSame(vacio, vacio.deshacer())
    }

    @Test
    fun `dado un indice fuera de rango cuando se quita entonces no cambia`() {
        val c = ConteoMarcas().agregar(0.1f, 0.1f)
        assertSame(c, c.quitar(5))
    }

    @Test
    fun `dadas marcas y ajuste cuando se limpia entonces se borran las marcas y se conserva el ajuste`() {
        // Arrange
        val c = ConteoMarcas().agregar(0.1f, 0.1f).agregar(0.2f, 0.2f).ajustar(3)

        // Act
        val l = c.limpiar()

        // Assert
        assertTrue(l.marcas.isEmpty())
        assertEquals(3, l.ajusteManual)
        assertEquals(3, l.total)
    }

    @Test
    fun `dado un total en 0 cuando se resta al ajuste entonces el total no baja de 0`() {
        // Act
        val c = ConteoMarcas().ajustar(-1)

        // Assert
        assertEquals(0, c.total)
        assertEquals(0, c.ajusteManual)
    }

    @Test
    fun `dado un ajuste negativo cuando se quitan marcas entonces el ajuste sube para que el total quede en 0`() {
        // Arrange: 2 marcas y ajuste −2 → total 0
        val c = ConteoMarcas().agregar(0.1f, 0.1f).agregar(0.2f, 0.2f).ajustar(-2)

        // Act
        val d = c.deshacer()

        // Assert
        assertEquals(0, d.total)
        assertEquals(-1, d.ajusteManual)
    }

    @Test
    fun `dadas marcas cuando se pasan a JSON y de vuelta entonces son las mismas`() {
        // Arrange
        val c = ConteoMarcas().agregar(0.25f, 0.6f).agregar(0.7f, 0.1f)

        // Act
        val vuelta = ConteoMarcas.marcasDesdeJson(c.aJson())

        // Assert
        assertEquals(c.marcas, vuelta)
    }

    @Test
    fun `dado un JSON vacio o danado cuando se leen las marcas entonces devuelve lista vacia`() {
        assertTrue(ConteoMarcas.marcasDesdeJson(null).isEmpty())
        assertTrue(ConteoMarcas.marcasDesdeJson("  ").isEmpty())
        assertTrue(ConteoMarcas.marcasDesdeJson("{esto no es json").isEmpty())
    }
}
