package com.aldemar.aquamuestra.data.reporte

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.aldemar.aquamuestra.viewmodel.FilaReporte
import com.aldemar.aquamuestra.viewmodel.Reporte
import java.io.OutputStream
import java.util.Locale

/**
 * Dibuja un [Reporte] como PDF con la API de Android (PdfDocument), sin librerías externas.
 *
 * Página A4 horizontal (842 × 595 puntos) para que la tabla de 9 columnas sea legible.
 * La paginación es automática: se calcula cuántas filas caben, se reparte en páginas y en cada
 * página se repite el encabezado de la tabla. El pie dice "página X de Y".
 */
object GeneradorPdf {

    // Medidas en puntos PDF (1/72 de pulgada)
    private const val ANCHO = 842
    private const val ALTO = 595
    private const val MARGEN = 32f
    private const val ALTO_FILA = 18f
    private const val ALTO_ENCABEZADO_TABLA = 20f
    private const val ALTO_PIE = 28f
    private const val ALTO_TOTALES = 64f

    private const val PIE = "Datos sintéticos para uso académico"

    /** Columnas: título y ancho. Suman el ancho útil (842 - 2 × 32 = 778). */
    private val columnas = listOf(
        "ID" to 58f, "Fecha" to 80f, "Centro" to 132f, "Tren" to 104f, "Línea" to 62f,
        "Tramo (cm)" to 60f, "Conteo" to 52f, "Observaciones" to 170f, "Estado" to 60f
    )

    private val tinta = Color.rgb(0x1B, 0x1F, 0x24)
    private val tintaSuave = Color.rgb(0x5A, 0x63, 0x6E)
    private val acento = Color.rgb(0x0B, 0x5C, 0x7A)
    private val fondoEncabezado = Color.rgb(0xE3, 0xEE, 0xF3)
    private val fondoAlterno = Color.rgb(0xF6, 0xF8, 0xFA)
    private val linea = Color.rgb(0xD0, 0xD7, 0xDE)

    private fun paint(tamano: Float, color: Int = tinta, negrita: Boolean = false) = Paint().apply {
        isAntiAlias = true
        textSize = tamano
        this.color = color
        typeface = Typeface.create(Typeface.SANS_SERIF, if (negrita) Typeface.BOLD else Typeface.NORMAL)
    }

    /** Escribe el PDF en [destino] (no lo cierra). */
    fun escribir(reporte: Reporte, destino: OutputStream) {
        val doc = PdfDocument()
        try {
            val paginas = paginar(reporte)
            paginas.forEachIndexed { i, filas ->
                val pagina = doc.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, i + 1).create())
                val c = pagina.canvas
                var y = MARGEN

                if (i == 0) y = dibujarEncabezadoReporte(c, reporte, y)
                // Una página que solo trae los totales no repite el encabezado de la tabla
                if (filas.isNotEmpty()) y = dibujarEncabezadoTabla(c, y)
                filas.forEachIndexed { j, fila -> y = dibujarFila(c, fila, y, alterna = j % 2 == 1) }
                if (i == paginas.lastIndex) dibujarTotales(c, reporte, y + 10f)
                dibujarPie(c, i + 1, paginas.size)

                doc.finishPage(pagina)
            }
            doc.writeTo(destino)
        } finally {
            doc.close()
        }
    }

    /** Reparte las filas en páginas: la primera tiene el encabezado del reporte; la última, los totales. */
    private fun paginar(reporte: Reporte): List<List<FilaReporte>> {
        val altoEncabezadoReporte = 70f + reporte.filtros.size * 14f
        val util = ALTO - 2 * MARGEN - ALTO_PIE - ALTO_ENCABEZADO_TABLA
        val primera = ((util - altoEncabezadoReporte) / ALTO_FILA).toInt().coerceAtLeast(1)
        val resto = (util / ALTO_FILA).toInt()

        val paginas = mutableListOf<List<FilaReporte>>()
        var pendientes = reporte.filas
        var capacidad = primera
        while (pendientes.isNotEmpty()) {
            paginas += pendientes.take(capacidad)
            pendientes = pendientes.drop(capacidad)
            capacidad = resto
        }
        // Si los totales no caben debajo de la última fila, van solos en una página más.
        val ultimas = paginas.last().size
        val capacidadUltima = if (paginas.size == 1) primera else resto
        if ((capacidadUltima - ultimas) * ALTO_FILA < ALTO_TOTALES) paginas += emptyList<FilaReporte>()
        return paginas
    }

    private fun dibujarEncabezadoReporte(c: android.graphics.Canvas, r: Reporte, yInicial: Float): Float {
        var y = yInicial + 16f
        c.drawText(r.titulo, MARGEN, y, paint(16f, acento, negrita = true))
        y += 20f
        val dato = paint(10f, tintaSuave)
        c.drawText("Generado el ${r.fechaGeneracion}  ·  por ${r.generadoPor}", MARGEN, y, dato)
        y += 16f
        c.drawText("Filtros aplicados:", MARGEN, y, paint(10f, tinta, negrita = true))
        r.filtros.forEach { f ->
            y += 14f
            c.drawText("• $f", MARGEN + 8f, y, dato)
        }
        return y + 18f
    }

    private fun dibujarEncabezadoTabla(c: android.graphics.Canvas, y: Float): Float {
        c.drawRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_ENCABEZADO_TABLA, Paint().apply { color = fondoEncabezado })
        val p = paint(9f, tinta, negrita = true)
        var x = MARGEN
        columnas.forEach { (titulo, ancho) ->
            c.drawText(titulo, x + 4f, y + 13.5f, p)
            x += ancho
        }
        return y + ALTO_ENCABEZADO_TABLA
    }

    private fun dibujarFila(c: android.graphics.Canvas, f: FilaReporte, y: Float, alterna: Boolean): Float {
        if (alterna) c.drawRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_FILA, Paint().apply { color = fondoAlterno })
        val p = paint(8.5f)
        val valores = listOf(
            f.id, f.fecha, f.centro, f.tren, f.linea,
            f.tramoCm.toString(), f.conteo.toString(), f.observaciones, f.estado
        )
        var x = MARGEN
        valores.zip(columnas).forEach { (valor, columna) ->
            val ancho = columna.second
            c.drawText(ajustar(valor, ancho - 8f, p), x + 4f, y + 12.5f, p)
            x += ancho
        }
        c.drawLine(MARGEN, y + ALTO_FILA, ANCHO - MARGEN, y + ALTO_FILA, Paint().apply { color = linea; strokeWidth = 0.5f })
        return y + ALTO_FILA
    }

    private fun dibujarTotales(c: android.graphics.Canvas, r: Reporte, yInicial: Float) {
        val t = r.totales
        var y = yInicial + 12f
        c.drawText("Totales", MARGEN, y, paint(11f, acento, negrita = true))
        val p = paint(10f)
        y += 16f
        c.drawText(
            "Muestras validadas: ${t.muestras}   ·   Individuos contados: ${t.individuos}   ·   " +
                "Metros muestreados: ${"%.2f".format(Locale.US, t.metrosMuestreados)} m",
            MARGEN, y, p
        )
        y += 15f
        val densidad = t.densidadPromedio?.let { "%.1f ind/m".format(Locale.US, it) } ?: "sin datos"
        c.drawText("Densidad promedio: $densidad", MARGEN, y, p)
    }

    private fun dibujarPie(c: android.graphics.Canvas, numero: Int, total: Int) {
        val y = ALTO - MARGEN + 6f
        c.drawLine(MARGEN, y - 14f, ANCHO - MARGEN, y - 14f, Paint().apply { color = linea; strokeWidth = 0.5f })
        val p = paint(8.5f, tintaSuave)
        val texto = "$PIE · página $numero de $total"
        c.drawText(texto, (ANCHO - p.measureText(texto)) / 2f, y, p)
    }

    /** Corta el texto con "…" si no cabe en el ancho de la columna. */
    private fun ajustar(texto: String, ancho: Float, p: Paint): String {
        if (p.measureText(texto) <= ancho) return texto
        val cabe = p.breakText(texto, true, ancho - p.measureText("…"), null)
        return texto.take(cabe).trimEnd() + "…"
    }
}
