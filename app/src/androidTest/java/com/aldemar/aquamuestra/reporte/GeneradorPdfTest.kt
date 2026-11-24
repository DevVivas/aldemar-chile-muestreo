package com.aldemar.aquamuestra.reporte

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aldemar.aquamuestra.data.reporte.GeneradorPdf
import com.aldemar.aquamuestra.viewmodel.FilaReporte
import com.aldemar.aquamuestra.viewmodel.Reporte
import com.aldemar.aquamuestra.viewmodel.TotalesReporte
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * GeneradorPdf usa PdfDocument (Android), por eso se prueba en el emulador:
 * se genera el PDF y se cuentan sus páginas con PdfRenderer.
 * Con 2 filtros caben 21 filas en la página 1 y 26 en las siguientes.
 */
@RunWith(AndroidJUnit4::class)
class GeneradorPdfTest {

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    private fun reporte(filas: Int) = Reporte(
        titulo = "AquaMuestra · Reporte de muestras validadas",
        fechaGeneracion = "24-11-2026 21:00",
        generadoPor = "Supervisor Demo (Supervisor de Calidad)",
        filtros = listOf("Período: todo", "Centro: todos"),
        filas = List(filas) { i ->
            FilaReporte(
                id = "%08x".format(i), fecha = "20-11-2026 10:30", centro = "Calbuco Sur",
                tren = "Tren 1 - Módulo A", linea = "Línea ${i % 20}", tramoCm = 100, conteo = 100 + i,
                observaciones = "Observación de prueba número $i con texto largo para recortar", estado = "Validada"
            )
        },
        totales = TotalesReporte(filas, filas * 100, filas.toDouble(), 100.0)
    )

    /** Genera el PDF en la carpeta de la app y devuelve cuántas páginas tiene. */
    private fun paginas(filas: Int): Int {
        val archivo = File(contexto.getExternalFilesDir(null), "prueba-paginacion-$filas.pdf")
        archivo.outputStream().use { GeneradorPdf.escribir(reporte(filas), it) }
        ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { return it.pageCount }
        }
    }

    @Test
    fun dadas3Filas_cuandoSeGenera_entoncesCabeEnUnaPagina() {
        assertEquals(1, paginas(3))
    }

    @Test
    fun dadas60Filas_cuandoSeGenera_entoncesUsaTresPaginasConTotalesEnLaUltima() {
        // 21 + 26 + 13 filas; debajo de las 13 sobra espacio para los totales
        assertEquals(3, paginas(60))
    }

    @Test
    fun dadas47Filas_cuandoLaUltimaPaginaQuedaLlena_entoncesLosTotalesVanEnUnaPaginaNueva() {
        // 21 + 26 = 47: la página 2 queda llena, los totales pasan a la página 3
        assertEquals(3, paginas(47))
    }
}
