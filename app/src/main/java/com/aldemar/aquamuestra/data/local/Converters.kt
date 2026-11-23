package com.aldemar.aquamuestra.data.local

import androidx.room.TypeConverter

/**
 * Room solo sabe guardar tipos simples (texto, números...).
 * Estos convertidores transforman el enum en texto al guardar y de vuelta al leer.
 */
class Converters {

    @TypeConverter
    fun estadoATexto(estado: EstadoRevision): String = estado.name

    @TypeConverter
    fun textoAEstado(valor: String): EstadoRevision = EstadoRevision.valueOf(valor)
}
