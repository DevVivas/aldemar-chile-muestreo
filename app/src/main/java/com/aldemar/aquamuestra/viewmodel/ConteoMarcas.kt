package com.aldemar.aquamuestra.viewmodel

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Una marca sobre la foto. [x] e [y] van de 0 a 1 y son relativos a la IMAGEN, no a la pantalla:
 * (0,0) es la esquina superior izquierda de la foto y (1,1) la inferior derecha.
 * Así la marca queda en el mismo lugar de la foto aunque se haga zoom o se desplace.
 */
data class Punto(val x: Float, val y: Float)

/**
 * Estado del conteo asistido. Kotlin puro e inmutable: cada operación devuelve un ConteoMarcas nuevo.
 * No depende de Android ni de Compose, así que se prueba con JUnit.
 *
 * total = marcas + ajusteManual, y nunca baja de 0.
 */
data class ConteoMarcas(
    val marcas: List<Punto> = emptyList(),
    val ajusteManual: Int = 0
) {
    val conteoMarcas: Int get() = marcas.size
    val total: Int get() = conteoMarcas + ajusteManual

    /** Agrega una marca en (x, y). Fuera de la imagen (fuera de 0..1) no hace nada. */
    fun agregar(x: Float, y: Float): ConteoMarcas =
        if (x !in 0f..1f || y !in 0f..1f) this else copy(marcas = marcas + Punto(x, y)).ajustado()

    /**
     * Toque sobre la foto: si cae sobre una marca existente la quita; si no, agrega una nueva.
     *
     * "Cae sobre una marca" se mide como una elipse con radios [toleranciaX] y [toleranciaY]
     * (también normalizados), porque la foto puede no ser cuadrada: el mismo radio en pantalla
     * equivale a fracciones distintas del ancho y del alto.
     *
     * @return el nuevo conteo y si se agregó (true) o se quitó (false) una marca.
     */
    fun tocar(x: Float, y: Float, toleranciaX: Float, toleranciaY: Float): Pair<ConteoMarcas, Boolean> {
        val cercana = indiceMasCercano(x, y, toleranciaX, toleranciaY)
        return if (cercana != null) quitar(cercana) to false else agregar(x, y) to true
    }

    /** Índice de la marca más cercana dentro de la tolerancia, o null si no hay ninguna. */
    fun indiceMasCercano(x: Float, y: Float, toleranciaX: Float, toleranciaY: Float): Int? {
        if (toleranciaX <= 0f || toleranciaY <= 0f) return null
        return marcas.withIndex()
            .map { (i, p) ->
                val dx = (p.x - x) / toleranciaX
                val dy = (p.y - y) / toleranciaY
                i to (dx * dx + dy * dy)
            }
            .filter { it.second <= 1f }
            .minByOrNull { it.second }
            ?.first
    }

    fun quitar(indice: Int): ConteoMarcas =
        if (indice !in marcas.indices) this else copy(marcas = marcas.filterIndexed { i, _ -> i != indice }).ajustado()

    /** Quita la última marca agregada. */
    fun deshacer(): ConteoMarcas = if (marcas.isEmpty()) this else copy(marcas = marcas.dropLast(1)).ajustado()

    /** Borra todas las marcas (el ajuste manual se conserva). */
    fun limpiar(): ConteoMarcas = copy(marcas = emptyList()).ajustado()

    /** Suma o resta al ajuste manual. El total nunca queda negativo. */
    fun ajustar(delta: Int): ConteoMarcas = copy(ajusteManual = ajusteManual + delta).ajustado()

    /** Si el total quedara negativo, sube el ajuste lo justo para dejarlo en 0. */
    private fun ajustado(): ConteoMarcas =
        if (total < 0) copy(ajusteManual = -conteoMarcas) else this

    fun aJson(): String = gson.toJson(marcas)

    companion object {
        private val gson = Gson()
        private val tipoLista = object : TypeToken<List<Punto>>() {}.type

        /** Lee las marcas guardadas en Room. Si el texto está vacío o dañado, devuelve una lista vacía. */
        fun marcasDesdeJson(json: String?): List<Punto> =
            if (json.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    gson.fromJson<List<Punto>>(json, tipoLista).orEmpty()
                } catch (e: Exception) {
                    emptyList()
                }
            }
    }
}
