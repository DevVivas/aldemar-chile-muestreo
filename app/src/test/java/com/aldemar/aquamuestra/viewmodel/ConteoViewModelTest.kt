package com.aldemar.aquamuestra.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConteoViewModelTest {

    private val tol = 0.05f

    @Test
    fun `dada una muestra con marcas cuando se inicia dos veces entonces solo vale la primera`() {
        // Arrange
        val vm = ConteoViewModel()

        // Act
        vm.iniciar("/fotos/a.jpg", """[{"x":0.1,"y":0.1}]""", ajusteManual = 3)
        vm.iniciar("/otra.jpg", null, ajusteManual = 0) // p. ej. al rotar la pantalla

        // Assert
        val s = vm.uiState.value
        assertEquals("/fotos/a.jpg", s.fotoPath)
        assertEquals(1, s.conteo.conteoMarcas)
        assertEquals(4, s.conteo.total)
    }

    @Test
    fun `dado un toque en un lugar vacio y otro sobre la marca entonces agrega y luego quita`() {
        // Arrange
        val vm = ConteoViewModel()
        vm.iniciar(null, null, 0)

        // Act + Assert: agregar
        vm.tocar(0.5f, 0.5f, tol, tol)
        assertEquals(1, vm.uiState.value.conteo.conteoMarcas)
        assertEquals(CambioConteo.AGREGADA, vm.uiState.value.ultimoCambio)

        // Act + Assert: quitar tocando encima
        vm.tocar(0.51f, 0.5f, tol, tol)
        assertEquals(0, vm.uiState.value.conteo.conteoMarcas)
        assertEquals(CambioConteo.QUITADA, vm.uiState.value.ultimoCambio)
    }

    @Test
    fun `dado un toque fuera de la foto cuando se toca entonces no cambia nada`() {
        // Arrange
        val vm = ConteoViewModel()

        // Act
        vm.tocar(1.5f, 0.5f, tol, tol)

        // Assert
        assertEquals(0, vm.uiState.value.conteo.conteoMarcas)
        assertNull(vm.uiState.value.ultimoCambio)
    }

    @Test
    fun `dadas marcas cuando se deshace se limpia y se ajusta entonces registra cada cambio`() {
        // Arrange
        val vm = ConteoViewModel()
        vm.deshacer() // sin marcas: no hace nada
        assertNull(vm.uiState.value.ultimoCambio)
        vm.tocar(0.1f, 0.1f, tol, tol)
        vm.tocar(0.9f, 0.9f, tol, tol)

        // Act + Assert
        vm.deshacer()
        assertEquals(1, vm.uiState.value.conteo.conteoMarcas)
        assertEquals(CambioConteo.DESHECHA, vm.uiState.value.ultimoCambio)

        vm.ajustar(+2)
        assertEquals(CambioConteo.AJUSTE, vm.uiState.value.ultimoCambio)
        assertEquals(3, vm.uiState.value.conteo.total)

        vm.onCambioMostrado()
        assertNull(vm.uiState.value.ultimoCambio)
        vm.ajustar(-10) // el total no baja de 0
        assertEquals(0, vm.uiState.value.conteo.total)
        vm.onCambioMostrado()
        vm.ajustar(-1) // ya está en 0: no cambia nada
        assertNull(vm.uiState.value.ultimoCambio)

        vm.limpiar()
        assertEquals(0, vm.uiState.value.conteo.conteoMarcas)
        assertEquals(CambioConteo.LIMPIADO, vm.uiState.value.ultimoCambio)
    }

    @Test
    fun `dado un conteo terminado cuando se pide el resultado entonces devuelve marcas ajuste y JSON`() {
        // Arrange
        val vm = ConteoViewModel()
        vm.tocar(0.2f, 0.3f, tol, tol)
        vm.ajustar(5)

        // Act
        val r = vm.resultado()

        // Assert
        assertEquals(1, r.conteoMarcas)
        assertEquals(5, r.ajusteManual)
        assertEquals(1, ConteoMarcas.marcasDesdeJson(r.marcasJson).size)
    }
}
