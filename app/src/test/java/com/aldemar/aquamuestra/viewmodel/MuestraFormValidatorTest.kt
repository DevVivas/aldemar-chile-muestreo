package com.aldemar.aquamuestra.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MuestraFormValidatorTest {

    /** Formulario completo y correcto; cada test cambia solo lo que quiere probar. */
    private fun validar(
        centro: String = "Calbuco Sur",
        tren: String = "Tren 1",
        linea: String = "Línea 10",
        operador: String = "Operario Demo",
        tramoCm: String = "100",
        cantidad: String = "150",
        observaciones: String = "",
        fotoPath: String? = "/fotos/m-1.jpg",
        paraEnviar: Boolean = true
    ) = MuestraFormValidator.validar(centro, tren, linea, operador, tramoCm, cantidad, observaciones, fotoPath, paraEnviar)

    /*
     * 🎯 TEST PARA LA DEMO
     * Entrada: un formulario completo, pero con el tramo en "0" cm.
     * Resultado esperado: el validador marca SOLO el campo tramo con ENTERO_MAYOR_A_CERO
     * (no se puede contar individuos en una cuerda de 0 cm) y el formulario queda con errores.
     */
    @Test
    fun `dado un tramo de 0 cm cuando se valida entonces el tramo tiene error de mayor a cero`() {
        // Arrange: todo correcto salvo el tramo
        val tramo = "0"

        // Act
        val errores = validar(tramoCm = tramo)

        // Assert
        assertEquals(ErrorCampo.ENTERO_MAYOR_A_CERO, errores.tramoCm)
        assertTrue(errores.hayErrores)
        assertNull(errores.centro)
    }

    @Test
    fun `dado un formulario completo cuando se valida para enviar entonces no hay errores`() {
        // Act
        val errores = validar()

        // Assert
        assertFalse(errores.hayErrores)
    }

    @Test
    fun `dado centro tren linea y operador vacios cuando se valida entonces los cuatro son obligatorios`() {
        // Act
        val errores = validar(centro = " ", tren = "", linea = "", operador = "  ")

        // Assert
        assertEquals(ErrorCampo.OBLIGATORIO, errores.centro)
        assertEquals(ErrorCampo.OBLIGATORIO, errores.tren)
        assertEquals(ErrorCampo.OBLIGATORIO, errores.linea)
        assertEquals(ErrorCampo.OBLIGATORIO, errores.operador)
    }

    @Test
    fun `dado un tramo vacio cuando se valida entonces es obligatorio`() {
        assertEquals(ErrorCampo.OBLIGATORIO, validar(tramoCm = "").tramoCm)
    }

    @Test
    fun `dado un tramo de 1 cm cuando se valida entonces es valido`() {
        assertNull(validar(tramoCm = "1").tramoCm)
    }

    @Test
    fun `dado un tramo que no es numero cuando se valida entonces tiene error de mayor a cero`() {
        assertEquals(ErrorCampo.ENTERO_MAYOR_A_CERO, validar(tramoCm = "abc").tramoCm)
    }

    @Test
    fun `dada una cantidad vacia cuando se valida entonces es obligatoria`() {
        assertEquals(ErrorCampo.OBLIGATORIO, validar(cantidad = "").cantidad)
    }

    @Test
    fun `dada una cantidad negativa cuando se valida entonces tiene error de cero o mas`() {
        assertEquals(ErrorCampo.ENTERO_CERO_O_MAS, validar(cantidad = "-1").cantidad)
    }

    @Test
    fun `dada una cantidad de 0 cuando se valida entonces es valida`() {
        assertNull(validar(cantidad = "0").cantidad)
    }

    @Test
    fun `dadas observaciones de 300 caracteres cuando se valida entonces son validas`() {
        assertNull(validar(observaciones = "a".repeat(300)).observaciones)
    }

    @Test
    fun `dadas observaciones de 301 caracteres cuando se valida entonces son demasiado largas`() {
        assertEquals(ErrorCampo.DEMASIADO_LARGO, validar(observaciones = "a".repeat(301)).observaciones)
    }

    @Test
    fun `dado un formulario sin foto cuando se valida para enviar entonces la foto es obligatoria`() {
        assertEquals(ErrorCampo.FOTO_OBLIGATORIA, validar(fotoPath = null, paraEnviar = true).foto)
    }

    @Test
    fun `dado un formulario sin foto cuando se valida como borrador entonces no se exige foto`() {
        // Act
        val errores = validar(fotoPath = null, paraEnviar = false)

        // Assert
        assertNull(errores.foto)
        assertFalse(errores.hayErrores)
    }
}
