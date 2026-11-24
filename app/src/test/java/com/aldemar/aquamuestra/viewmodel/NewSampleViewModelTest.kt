package com.aldemar.aquamuestra.viewmodel

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.util.Fixtures.muestra
import com.aldemar.aquamuestra.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NewSampleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<MuestraRepository>(relaxed = true)
    private val zonaOriginal = TimeZone.getDefault()

    @Before
    fun zonaFija() = TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"))

    @After
    fun restaurarZona() = TimeZone.setDefault(zonaOriginal)

    private fun nuevoVm() = NewSampleViewModel(repo, reloj = { 1_000L }, generarId = { "id-nuevo" })

    /** Llena todos los campos obligatorios para enviar. */
    private fun NewSampleViewModel.llenarCompleto() {
        onCentroChange(" Calbuco Sur ")
        onConcesionChange("  ")
        onTrenChange("Tren 1 - Módulo A")
        onLineaChange("Línea 10")
        onOperadorChange("Operario Demo")
        onTramoCmChange("100")
        onConteoGuardado(conteoMarcas = 140, ajusteManual = 10, marcasJson = "[]")
        onObservacionesChange(" ok ")
        onFotoCapturada("/fotos/a.jpg")
        onConteoGuardado(conteoMarcas = 140, ajusteManual = 10, marcasJson = "[]")
    }

    @Test
    fun `dado un formulario nuevo cuando se abre entonces tiene id generado y la hora del reloj`() {
        // Act
        val s = nuevoVm().uiState.value

        // Assert
        assertEquals("id-nuevo", s.id)
        assertEquals(1_000L, s.fechaHora)
        assertNull(s.estadoOriginal)
    }

    @Test
    fun `dado el formulario vacio cuando se envia entonces marca los obligatorios y no guarda`() = runTest {
        // Arrange
        val vm = nuevoVm()

        // Act
        vm.enviar()

        // Assert
        val e = vm.uiState.value.errores
        assertEquals(ErrorCampo.OBLIGATORIO, e.centro)
        assertEquals(ErrorCampo.OBLIGATORIO, e.tren)
        assertEquals(ErrorCampo.OBLIGATORIO, e.linea)
        assertEquals(ErrorCampo.OBLIGATORIO, e.operador)
        assertEquals(ErrorCampo.OBLIGATORIO, e.tramoCm)
        assertEquals(ErrorCampo.FOTO_OBLIGATORIA, e.foto)
        assertEquals(NewSampleEvento.HayErrores, vm.uiState.value.evento)
        coVerify(exactly = 0) { repo.guardar(any()) }
    }

    @Test
    fun `dado un intento fallido cuando se corrige un campo entonces su error desaparece al instante`() {
        // Arrange
        val vm = nuevoVm()
        vm.enviar()

        // Act
        vm.onCentroChange("Calbuco Sur")

        // Assert
        assertNull(vm.uiState.value.errores.centro)
        assertEquals(ErrorCampo.OBLIGATORIO, vm.uiState.value.errores.tren)
    }

    @Test
    fun `dado un borrador sin foto cuando se guarda entonces se guarda como BORRADOR`() = runTest {
        // Arrange
        val vm = nuevoVm()
        vm.llenarCompleto()
        vm.onFotoQuitada()
        val guardada = slot<MuestraEntity>()
        coEvery { repo.guardar(capture(guardada)) } returns Unit

        // Act
        vm.guardarBorrador()

        // Assert
        assertEquals(EstadoRevision.BORRADOR, guardada.captured.estado)
        assertNull(guardada.captured.fotoPath)
        assertEquals(NewSampleEvento.GuardadoOk(EstadoRevision.BORRADOR), vm.uiState.value.evento)
    }

    @Test
    fun `dado un formulario completo cuando se envia entonces guarda los datos limpios como ENVIADA`() = runTest {
        // Arrange
        val vm = nuevoVm()
        vm.llenarCompleto()
        val guardada = slot<MuestraEntity>()
        coEvery { repo.guardar(capture(guardada)) } returns Unit

        // Act
        vm.enviar()

        // Assert
        with(guardada.captured) {
            assertEquals("id-nuevo", id)
            assertEquals("Calbuco Sur", centro) // sin espacios
            assertNull(concesion) // en blanco → null
            assertEquals(100, tramoCm)
            assertEquals(150, cantidad) // 140 marcas + 10 de ajuste
            assertEquals("ok", observaciones)
            assertEquals(EstadoRevision.ENVIADA, estado)
            assertNull(comentarioRevision)
        }
        assertFalse(vm.uiState.value.isSaving)
        assertEquals(NewSampleEvento.GuardadoOk(EstadoRevision.ENVIADA), vm.uiState.value.evento)

        // Act: la pantalla ya mostró el evento
        vm.onEventoMostrado()

        // Assert
        assertNull(vm.uiState.value.evento)
    }

    @Test
    fun `dado que Room falla cuando se envia entonces avisa error al guardar`() = runTest {
        // Arrange
        val vm = nuevoVm()
        vm.llenarCompleto()
        coEvery { repo.guardar(any()) } throws IllegalStateException("disco lleno")

        // Act
        vm.enviar()

        // Assert
        assertEquals(NewSampleEvento.ErrorAlGuardar, vm.uiState.value.evento)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `dada una muestra observada cuando se abre en modo edicion entonces carga sus datos`() = runTest {
        // Arrange
        coEvery { repo.obtenerPorId("m-1") } returns muestra(
            estado = EstadoRevision.OBSERVADA, tramoCm = 80, cantidad = 12, conteoMarcas = 10, ajusteManual = 2,
            fotoPath = "/fotos/m-1.jpg", comentarioRevision = "Repetir", revisadoPor = "Supervisor Demo"
        )

        // Act
        val vm = NewSampleViewModel(repo, muestraIdEditar = "m-1")

        // Assert
        val s = vm.uiState.value
        assertFalse(s.cargando)
        assertEquals("m-1", s.id)
        assertEquals(EstadoRevision.OBSERVADA, s.estadoOriginal)
        assertEquals("80", s.tramoCm)
        assertEquals(12, s.cantidad)
        assertEquals("/fotos/m-1.jpg", s.fotoPath)
        // Modo corrección: el formulario muestra arriba lo que pidió el supervisor y quién
        assertEquals("Repetir", s.comentarioRevision)
        assertEquals("Supervisor Demo", s.revisadoPor)
    }

    @Test
    fun `dada una muestra observada cuando se reenvia o se guarda en borrador entonces maneja el comentario del supervisor`() = runTest {
        // Arrange
        coEvery { repo.obtenerPorId("m-1") } returns muestra(
            estado = EstadoRevision.OBSERVADA, fotoPath = "/fotos/m-1.jpg",
            comentarioRevision = "Repetir", revisadoPor = "Supervisor Demo"
        )
        val guardadas = mutableListOf<MuestraEntity>()
        coEvery { repo.guardar(capture(guardadas)) } returns Unit
        val vm = NewSampleViewModel(repo, muestraIdEditar = "m-1")

        // Act
        vm.guardarBorrador()
        vm.enviar()

        // Assert: en borrador se conserva el comentario; al reenviar se limpia
        assertEquals("Repetir", guardadas[0].comentarioRevision)
        assertNull(guardadas[1].comentarioRevision)
        assertEquals("Supervisor Demo", guardadas[1].revisadoPor)
    }

    @Test
    fun `dado un id que ya no existe cuando se abre en modo edicion entonces deja de cargar con el formulario vacio`() = runTest {
        // Arrange
        coEvery { repo.obtenerPorId("borrada") } returns null

        // Act
        val vm = NewSampleViewModel(repo, muestraIdEditar = "borrada")

        // Assert
        assertFalse(vm.uiState.value.cargando)
        assertEquals("", vm.uiState.value.centro)
    }

    @Test
    fun `dado un tramo con letras cuando se escribe entonces solo quedan los digitos y maximo 6`() {
        // Arrange
        val vm = nuevoVm()

        // Act
        vm.onTramoCmChange("12a3b4567")

        // Assert
        assertEquals("123456", vm.uiState.value.tramoCm)
    }

    @Test
    fun `dado un operador escrito cuando se precarga la sesion entonces no lo reemplaza`() {
        // Arrange
        val vm = nuevoVm()

        // Act + Assert
        vm.precargarOperador("Operario Demo")
        assertEquals("Operario Demo", vm.uiState.value.operador)
        vm.precargarOperador("Otro")
        assertEquals("Operario Demo", vm.uiState.value.operador)
        vm.precargarOperador(null)
        assertEquals("Operario Demo", vm.uiState.value.operador)
    }

    @Test
    fun `dada una fecha y una hora elegidas cuando se cambian entonces se combinan en la zona local`() {
        // Arrange
        val vm = nuevoVm()
        val medianocheUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear(); set(2026, Calendar.NOVEMBER, 20)
        }.timeInMillis

        // Act
        vm.onFechaChange(medianocheUtc)
        vm.onHoraChange(10, 30)

        // Assert
        val c = Calendar.getInstance().apply { timeInMillis = vm.uiState.value.fechaHora }
        assertEquals(2026, c.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH))
        assertEquals(20, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, c.get(Calendar.MINUTE))
    }

    @Test
    fun `dadas marcas sobre una foto cuando se cambia la foto entonces se borran las marcas y el ajuste no queda negativo`() {
        // Arrange
        val vm = nuevoVm()
        vm.onConteoGuardado(conteoMarcas = 5, ajusteManual = -2, marcasJson = "[...]")

        // Act
        vm.onFotoCapturada("/fotos/nueva.jpg")

        // Assert
        val s = vm.uiState.value
        assertEquals(0, s.conteoMarcas)
        assertEquals(0, s.ajusteManual)
        assertNull(s.marcasJson)
        assertTrue(s.fotoPath == "/fotos/nueva.jpg")
    }
}
